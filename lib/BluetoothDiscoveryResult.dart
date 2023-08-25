part of flutter_bluetooth_serial_ble;

class BluetoothDiscoveryResult {
  final BluetoothDevice device;
  final int rssi;

  BluetoothDiscoveryResult({
    required this.device,
    this.rssi = 0,
  });

  factory BluetoothDiscoveryResult.fromMap(Map map) {
    return BluetoothDiscoveryResult(
      device: BluetoothDevice.fromMap(map),
      rssi: map['rssi'] ?? 0,
    );
  }
  @override
  bool operator ==(Object other) {
    if (identical(this, other)) {
      return true;
    }
    return (other is BluetoothDiscoveryResult &&
        other.runtimeType == runtimeType &&
        other.rssi == rssi &&
        other.device == device);
  }

  @override
  // TODO: implement hashCode
  int get hashCode => Object.hash(device, rssi);
  @override
  String toString()=>"BluetoothDiscoveryResult(device:$device,rssi: $rssi)";
}
