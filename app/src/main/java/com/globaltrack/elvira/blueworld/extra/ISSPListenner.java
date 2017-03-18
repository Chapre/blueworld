package com.globaltrack.elvira.blueworld.extra;

/**
 * Created by ELVIRA III on 2017/03/16.
 */

public interface ISSPListenner {
    void OnSSPPost(SSPClient client, byte[] data);
}
