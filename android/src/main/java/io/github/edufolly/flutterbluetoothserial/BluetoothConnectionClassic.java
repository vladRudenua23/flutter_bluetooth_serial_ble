package io.github.edufolly.flutterbluetoothserial;

import static android.content.ContentValues.TAG;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.Arrays;
import java.util.function.Consumer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/// Universal Bluetooth serial connection class (for Java)
public class BluetoothConnectionClassic extends BluetoothConnectionBase
{
    protected static final UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    protected BluetoothAdapter bluetoothAdapter;

    protected ConnectionThread connectionThread = null;

    public boolean isConnected() {
        return connectionThread != null && connectionThread.requestedClosing != true;
    }



    public BluetoothConnectionClassic(OnReadCallback onReadCallback, OnDisconnectedCallback onDisconnectedCallback, BluetoothAdapter bluetoothAdapter) {
        super(onReadCallback, onDisconnectedCallback);
        this.bluetoothAdapter = bluetoothAdapter;
    }



    // @TODO . `connect` could be done perfored on the other thread
    // @TODO . `connect` parameter: timeout
    // @TODO . `connect` other methods than `createRfcommSocketToServiceRecord`, including hidden one raw `createRfcommSocket` (on channel).
    // @TODO ? how about turning it into factoried?
    public void connect(String address, UUID uuid) throws Exception {
        if (isConnected()) {
            throw new IOException("already connected");
        }
        BluetoothSocket socket = null;
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            throw new IOException("device not found");
        }
        Method method = null;
        try {
            method =  device.getClass().getMethod("createRfcommSocket", int.class);
        } catch (NoSuchMethodException e) {
         System.out.println(e.getMessage());
        }
        if (method == null){
            System.out.println("methods are null");
            throw new Exception("method are null");
        }
        socket = (BluetoothSocket) method.invoke(device, 3);//

        // Cancel discovery, even though we didn't start it
        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();

        }
        if(socket == null){
            System.out.println("socket are null");
        }else {
            try {
                socket.connect();
            }catch (IOException e){
                Log.e(TAG, "Could not close the client socket", e);
                throw  e;
            }
            if(socket.isConnected()){
                System.out.println(" == connected ===");
            }else {
                System.out.println(" == not connected ===");
            }
            connectionThread = new ConnectionThread(socket);
            connectionThread.start();
        }
    }

    public void connect(String address) throws Exception {
        connect(address, DEFAULT_UUID);
    }
    
    public void disconnect() {
        if (isConnected()) {
            connectionThread.cancel();
            connectionThread = null;
        }
    }

    public void write(byte[] data) throws IOException {
        if (!isConnected()) {
            throw new IOException("not connected");
        }

        connectionThread.write(data);
    }

    /// Thread to handle connection I/O
    private class ConnectionThread extends Thread  {
        private final BluetoothSocket socket;
        private final InputStream input;
        private final OutputStream output;
        private byte[] mmBuffer;
        private boolean requestedClosing = false;
        
        ConnectionThread(BluetoothSocket socket) throws IOException {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();

            } catch (IOException e) {
                System.out.println("connection input "+e.getMessage());
            }
            try {
                tmpOut = socket.getOutputStream();
            }catch (IOException e){

                System.out.println("connection output "+e.getMessage());

            }            if(tmpIn !=null){
                System.out.println("tmp"+tmpIn.available());
            }
            this.input = tmpIn;
            this.output = tmpOut;
        }

        /// Thread main code

        public void run() {
            Log.i(TAG, "BEGIN BT Monitor");
            mmBuffer = new byte[1024];
            int bytes;
            System.out.println("input available try to start while");
            while (true) {
                System.out.print("buffer while");
                try {
                    bytes = input.read(mmBuffer);
                    System.out.print("bytes:"+bytes);
                    onRead(Arrays.copyOf(mmBuffer, bytes));
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    // `input.read` throws when closed by remote device
                    break;
                } finally {
                    System.out.print("buffer"+ Arrays.toString(mmBuffer));
                }
            }

            // Make sure output stream is closed
            if (output != null) {
                try {
                    output.close();
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            // Make sure input stream is closed
            try {
                input.close();
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // Callback on disconnected, with information which side is closing
            onDisconnected(!requestedClosing);

            // Just prevent unnecessary `cancel`ing
            requestedClosing = true;
        }

        /// Writes to output stream
        public void write(byte[] bytes) {
            try {
                output.write(bytes);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        /// Stops the thread, disconnects
        public void cancel() {
            if (requestedClosing) {
                return;
            }
            requestedClosing = true;

            // Flush output buffers befoce closing
            try {
                output.flush();
            }
            catch (Exception e) {
                System.out.println(e.getMessage());

            }

            // Close the connection socket
            if (socket != null) {
                try {
                    // Might be useful (see https://stackoverflow.com/a/22769260/4880243)
                    Thread.sleep(111);

                    socket.close();
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());

                }
            }
        }
    }
}
