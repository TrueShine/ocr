//
//  CameraService.swift
//  testOCR
//
//  Created by JinukRyu on 3/10/25.
//

import AVFoundation

class CameraService: NSObject, ObservableObject {
    var session = AVCaptureSession()
    
    override init() {
        super.init()
        checkPermissions()
    }
    
    private func checkPermissions() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            setupSession()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                if granted {
                    self.setupSession()
                }
            }
        default:
            break
        }
    }
    
    private func setupSession() {
        session.beginConfiguration()
        session.sessionPreset = .photo
        
        guard let camera = AVCaptureDevice.default(for: .video),
              let input = try? AVCaptureDeviceInput(device: camera),
              session.canAddInput(input) else {
            return
        }
        
        session.addInput(input)
        session.commitConfiguration()
        session.startRunning()
    }
}
