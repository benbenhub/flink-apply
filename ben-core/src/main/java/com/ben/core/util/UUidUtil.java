package com.ben.core.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.*;
import java.util.*;

/**
 * 使用方法：
 * 先创建setMiddleId()获取IP和进程ID（按照分区调用）
 * 调用getUUid()获取ID
 * @author learn
 * @date 2022/9/23 10:41
 */
public class UUidUtil {

    private Long lastTimestamp = -1L;

    private int seq = 1;
    private final int maxSeq = 256;
    private final long epoch = 1483200000000L;

    private String middleId;

    private static UUidUtil uUidUtil;

    public static Long getUUid() {
        return uUidUtil.nextUUid();
    }

    public Long nextUUid() {

        Long timestamp = System.currentTimeMillis();
        if (lastTimestamp.equals(timestamp)) {
            seq++;
            if (seq == maxSeq) {
                timestamp = untilNextMillis(lastTimestamp);
                seq = 0;
            }
        } else {
            seq = 0;
        }
        if (timestamp < lastTimestamp) {
            // 服务器系统时钟回调，主键生成报错
            throw new Error("Clock moved backwards.Refusing to generate id.this.lastTimestamp:" + lastTimestamp + " timestamp:" + timestamp);
        }
        lastTimestamp = timestamp;
        StringBuilder bitSeq = new StringBuilder(Integer.toString(seq, 2));
        while (bitSeq.length() < 8) {
            bitSeq.insert(0, "0");
        }
        String binStr = Long.toString((timestamp - epoch),2) + middleId + bitSeq;

        return Long.parseLong(binStr,2);
    }

    /**
     * 获取IP进程ID
     * 给MiddleId赋值
     */
    public static void setMiddleId(){
        try {
            uUidUtil = new UUidUtil();
            uUidUtil.middleId = uUidUtil.getIP12Bit() + uUidUtil.getPid4Bit();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private Long untilNextMillis(Long lastTimestamp){
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    private String getIP12Bit() throws SocketException {
        Optional<Inet4Address> getIpv4 = getLocalIp4Address();
        String ipv4 = getIpv4.isPresent() ? String.valueOf(getIpv4.get()) : "/127.0.0.1";
        ipv4 = ipv4.replace("/","");
        String[] ipArr = ipv4.split("\\.");
        StringBuilder ip8BitStr = new StringBuilder(Integer.toString(Integer.parseInt(ipArr[ipArr.length - 1]), 2));
        StringBuilder ip4BitStr = new StringBuilder(Integer.toString(Integer.parseInt(ipArr[ipArr.length - 2]), 2));

        if(ip8BitStr.length() > 8)
            ip8BitStr = new StringBuilder(ip4BitStr.substring(ip8BitStr.length() - 8));
        if(ip4BitStr.length() > 4)
            ip4BitStr = new StringBuilder(ip8BitStr.substring(ip4BitStr.length() - 4));

        while (ip8BitStr.length() < 8) ip8BitStr.insert(0, '0');
        while (ip4BitStr.length() < 4) ip4BitStr.insert(0, '0');

        return ip4BitStr.toString() + ip8BitStr.toString();
    }

    private String getPid4Bit(){
        String getPidBit = Integer.toString(getProcessID(),2);
        StringBuilder getPid4Bit = new StringBuilder(
                getPidBit.length() > 4 ? getPidBit.substring(getPidBit.length() - 4) : getPidBit
        );
        while (getPid4Bit.length() < 4) {
            getPid4Bit.insert(0, '0');
        }
        return getPid4Bit.toString();
    }

    public Integer getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.parseInt(runtimeMXBean.getName().split("@")[0]);
    }

    public Optional<Inet4Address> getLocalIp4Address() throws SocketException {
        final List<Inet4Address> inet4Addresses = getLocalIp4AddressFromNetworkInterface();
        if (inet4Addresses.size() != 1) {
            final Optional<Inet4Address> ipBySocketOpt = getIpBySocket();
            if (ipBySocketOpt.isPresent()) {
                return ipBySocketOpt;
            } else {
                return inet4Addresses.isEmpty() ? Optional.empty() : Optional.of(inet4Addresses.get(0));
            }
        }
        return Optional.of(inet4Addresses.get(0));
    }

    /*
     * 获取本机所有网卡信息   得到所有IP信息
     * @return Inet4Address>
     */
    public List<Inet4Address> getLocalIp4AddressFromNetworkInterface() throws SocketException {
        List<Inet4Address> addresses = new ArrayList<>(1);

        // 所有网络接口信息
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        if (Objects.isNull(networkInterfaces)) {
            return addresses;
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            //滤回环网卡、点对点网卡、非活动网卡、虚拟网卡并要求网卡名字是eth或ens开头
            if (!isValidInterface(networkInterface)) {
                continue;
            }

            // 所有网络接口的IP地址信息
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                // 判断是否是IPv4，并且内网地址并过滤回环地址.
                if (isValidAddress(inetAddress)) {
                    addresses.add((Inet4Address) inetAddress);
                }
            }
        }
        return addresses;
    }
    /**
     * 判断是否是IPv4，并且内网地址并过滤回环地址.
     */
    private boolean isValidAddress(InetAddress address) {
        return address instanceof Inet4Address && address.isSiteLocalAddress() && !address.isLoopbackAddress();
    }
    /*
     * 通过Socket 唯一确定一个IP
     * 当有多个网卡的时候，使用这种方式一般都可以得到想要的IP。甚至不要求外网地址8.8.8.8是可连通的
     * @return Inet4Address>
     */
    private Optional<Inet4Address> getIpBySocket() throws SocketException {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            if (socket.getLocalAddress() instanceof Inet4Address) {
                return Optional.of((Inet4Address) socket.getLocalAddress());
            }
        } catch (UnknownHostException networkInterfaces) {
            throw new RuntimeException(networkInterfaces);
        }
        return Optional.empty();
    }
    /**
     * 过滤回环网卡、点对点网卡、非活动网卡、虚拟网卡并要求网卡名字是eth或ens开头
     *
     * @param ni 网卡
     * @return 如果满足要求则true，否则false
     */
    private boolean isValidInterface(NetworkInterface ni) throws SocketException {
        return !ni.isLoopback() && !ni.isPointToPoint() && ni.isUp() && !ni.isVirtual()
                && (ni.getName().startsWith("eth") || ni.getName().startsWith("ens"));
    }
}
