import 'package:flutter/material.dart';
import '../services/uhf_rfid_manager.dart';
import '../models/reader_info.dart';
import '../utils/colors.dart';
import '../utils/helpers.dart';

class SettingsDialog extends StatefulWidget {
  final UHFRFIDManager rfidManager;
  final ReaderInfo? currentSettings;
  final Function(String band, int power) onSettingsChanged;

  const SettingsDialog({
    super.key,
    required this.rfidManager,
    required this.currentSettings,
    required this.onSettingsChanged,
  });

  @override
  State<SettingsDialog> createState() => _SettingsDialogState();
}

class _SettingsDialogState extends State<SettingsDialog> {
  late String _selectedBand;
  late int _selectedPower;
  bool _isApplying = false;

  @override
  void initState() {
    super.initState();
    _selectedBand = widget.currentSettings?.band ?? 'U';
      final powerRange = widget.rfidManager.getPowerRange();
  final currentPower = widget.currentSettings?.power ?? 20;
    _selectedPower = currentPower.clamp(powerRange['min']!, powerRange['max']!);

    if (currentPower != _selectedPower) {
    debugPrint('⚠️ Current power ($currentPower) outside valid range, clamped to $_selectedPower');
  }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
      ),
      child: ConstrainedBox(
        constraints: BoxConstraints(
          maxHeight: MediaQuery.of(context).size.height * 0.85, // Limit dialog height
          maxWidth: 400,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Fixed Header
            Container(
              padding: const EdgeInsets.all(24),
              child: _buildHeader(),
            ),
            
            // Scrollable Content
            Flexible(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _buildBandSection(),
                    const SizedBox(height: 20),
                    _buildPowerSection(),
                  ],
                ),
              ),
            ),
            
            // Fixed Footer
            Container(
              padding: const EdgeInsets.all(24),
              child: _buildButtons(context),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Row(
      children: [
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Colors.blue.withOpacity(0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: const Icon(
            Icons.settings,
            color: Colors.blue,
            size: 24,
          ),
        ),
        const SizedBox(width: 16),
        const Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Reader Settings',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              Text(
                'Configure band and power settings',
                style: TextStyle(
                  fontSize: 13,
                  color: Colors.grey,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildBandSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Frequency Band',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 12),
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Colors.grey[50],
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: Colors.grey[200]!),
          ),
          child: Column(
            children: widget.rfidManager.getAvailableBands().map((band) {
              return Container(
                margin: const EdgeInsets.symmetric(vertical: 2),
                child: RadioListTile<String>(
                  title: Text(
                    band,
                    style: const TextStyle(
                      fontWeight: FontWeight.w600,
                      fontSize: 14,
                    ),
                  ),
                  subtitle: Text(
                    widget.rfidManager.getBandDisplayName(band),
                    style: const TextStyle(fontSize: 11),
                  ),
                  value: band,
                  groupValue: _selectedBand,
                  onChanged: (value) {
                    setState(() {
                      _selectedBand = value!;
                    });
                  },
                  activeColor: Colors.blue,
                  contentPadding: const EdgeInsets.symmetric(horizontal: 8),
                  dense: true,
                ),
              );
            }).toList(),
          ),
        ),
      ],
    );
  }

Widget _buildPowerSection() {
  final powerRange = widget.rfidManager.getPowerRange();
  final deviceType = widget.rfidManager.isMU910 ? 'MU910' : 'MU903';
  
  return Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          const Text(
            'Transmission Power',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(
              horizontal: 10,
              vertical: 4,
            ),
            decoration: BoxDecoration(
              color: Colors.blue.withOpacity(0.1),
              borderRadius: BorderRadius.circular(6),
              border: Border.all(
                color: Colors.blue.withOpacity(0.3),
              ),
            ),
            child: Text(
              '${_selectedPower} dB',
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.bold,
                color: Colors.blue,
              ),
            ),
          ),
        ],
      ),
      const SizedBox(height: 12),
      Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.grey[50],
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.grey[200]!),
        ),
        child: Column(
          children: [
            Slider(
              value: _selectedPower.toDouble(),
              min: powerRange['min']!.toDouble(),
              max: powerRange['max']!.toDouble(),
              divisions: powerRange['max']! - powerRange['min']!,
              label: '${_selectedPower} dB',
              onChanged: (value) {
                setState(() {
                  _selectedPower = value.round();
                });
              },
              activeColor: Colors.blue,
            ),
            const SizedBox(height: 4),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Min: ${powerRange['min']} dB',
                  style: const TextStyle(
                    fontSize: 11,
                    color: Colors.grey,
                  ),
                ),
                Text(
                  'Max: ${powerRange['max']} dB ($deviceType)',
                  style: const TextStyle(
                    fontSize: 11,
                    color: Colors.grey,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
      const SizedBox(height: 8),
      Container(
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(
          color: Colors.amber.withOpacity(0.1),
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.amber.withOpacity(0.3)),
        ),
        child: Row(
          children: [
            const Icon(
              Icons.warning_amber,
              color: Colors.amber,
              size: 16,
            ),
            const SizedBox(width: 8),
            Expanded(
              child: Text(
                'Higher power increases read range but may cause interference. $deviceType supports ${powerRange['min']}-${powerRange['max']}dB.',
                style: const TextStyle(
                  fontSize: 11,
                  color: Colors.amber,
                ),
              ),
            ),
          ],
        ),
      ),
    ],
  );
}

  Widget _buildButtons(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.end,
      children: [
        TextButton(
          onPressed: _isApplying ? null : () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        const SizedBox(width: 12),
        ElevatedButton.icon(
          onPressed: _isApplying ? null : _applySettings,
          icon: _isApplying
              ? const SizedBox(
                  width: 14,
                  height: 14,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Colors.white,
                  ),
                )
              : const Icon(Icons.check, size: 16),
          label: Text(
            _isApplying ? 'Applying...' : 'Apply',
            style: const TextStyle(fontSize: 13),
          ),
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.blue,
            foregroundColor: Colors.white,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
          ),
        ),
      ],
    );
  }

Future<void> _applySettings() async {
  setState(() {
    _isApplying = true;
  });

  try {
    // First check what features are supported
    final supportedFeatures = await widget.rfidManager.getSupportedFeatures();
    
    bool success = false;
    String message = '';
    
    if (!supportedFeatures['bandChange']! && _selectedBand != widget.currentSettings?.band) {
      // Device doesn't support band changes
      message = 'This device only supports power changes, not band changes';
      success = false;
    } else if (!supportedFeatures['powerChange']! && _selectedPower != widget.currentSettings?.power) {
      // Device doesn't support power changes
      message = 'This device does not support settings changes';
      success = false;
    } else {
      // Try to apply the settings
      success = await widget.rfidManager.setBandAndPower(
        _selectedBand,
        _selectedPower,
      );
      
      if (success) {
        message = 'Settings applied successfully';
      } else {
        message = 'Failed to apply settings - device may not support these changes';
      }
    }

    if (mounted) {
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Row(
            children: [
              Icon(
                success ? Icons.check_circle : Icons.error, 
                color: Colors.white, 
                size: 20
              ),
              const SizedBox(width: 12),
              Expanded(child: Text(message)),
            ],
          ),
          backgroundColor: success ? Colors.green : Colors.orange,
          behavior: SnackBarBehavior.floating,
          margin: const EdgeInsets.all(16),
          shape: const RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(8)),
          ),
        ),
      );
      
      if (success) {
        widget.onSettingsChanged(_selectedBand, _selectedPower);
      }
    }
  } catch (e) {
    if (mounted) {
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Row(
            children: [
              const Icon(Icons.error, color: Colors.white, size: 20),
              const SizedBox(width: 12),
              Expanded(child: Text('Error: $e')),
            ],
          ),
          backgroundColor: Colors.red,
          behavior: SnackBarBehavior.floating,
          margin: const EdgeInsets.all(16),
          shape: const RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(8)),
          ),
        ),
      );
    }
  } finally {
    if (mounted) {
      setState(() {
        _isApplying = false;
      });
    }
  }
}
}