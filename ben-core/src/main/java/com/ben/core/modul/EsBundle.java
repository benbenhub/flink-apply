package com.ben.core.modul;

public class EsBundle<T> {

    public enum EsAction{
        INSERT,DELETE,UPDATA
    }

    private String index;
    /*
    * Es单挑数据id
    * */
    private String id;
    /**
     * 数据本体
     * */
    private T data;
    /**
     * 执行动作，删除，增加，修改
     * 默认为增加操作
     * */
    EsAction action = EsAction.INSERT;

    public EsBundle() {
    }

    public EsBundle(String index, String id, T data, EsAction action) {
        this.index = index;
        this.id = id;
        this.data = data;
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public EsBundle<T> setId(String id) {
        this.id = id;
        return this;
    }

    public T getData() {
        return data;
    }

    public EsBundle<T> addData(T data) {
        this.data = data;
        return this;
    }

    public EsAction getAction() {
        return action;
    }

    public EsBundle<T> setAction(EsAction action) {
        this.action = action;
        return this;
    }

    public String getIndex() {
        return index;
    }

    public EsBundle<T> setIndex(String index) {
        this.index = index;
        return this;
    }

    @Override
    public String toString() {
        return "EsBundle{" +
                "index='" + index + '\'' +
                ", id='" + id + '\'' +
                ", data=" + data +
                ", action=" + action +
                '}';
    }
}
