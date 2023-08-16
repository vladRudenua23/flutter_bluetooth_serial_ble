package io.github.edufolly.flutterbluetoothserial;

public abstract class BluetoothConnectionBase implements io.github.edufolly.flutterbluetoothserial.BluetoothConnection {
    public interface OnReadCallback {
        public void onRead(byte[] data);
    }
    public interface OnDisconnectedCallback {
        public void onDisconnected(boolean byRemote);
    }

    final OnReadCallback onReadCallback;
    final OnDisconnectedCallback onDisconnectedCallback;

    public BluetoothConnectionBase(OnReadCallback onReadCallback, OnDisconnectedCallback onDisconnectedCallback) {
        this.onReadCallback = onReadCallback;
        this.onDisconnectedCallback = onDisconnectedCallback;
    }

    public void onRead(byte[] data) {
        onReadCallback.onRead(data);
    }

    public void onDisconnected(boolean byRemote) {
        onDisconnectedCallback.onDisconnected(byRemote);
    }
}