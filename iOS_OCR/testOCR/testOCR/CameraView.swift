//
//  CameraView.swift
//  testOCR
//
//  Created by JinukRyu on 3/10/25.
//

import SwiftUI

struct CameraView: View {
    @StateObject private var cameraService = CameraService()
    
    var body: some View {
        ZStack {
            CameraPreview(session: cameraService.session)
                .ignoresSafeArea()
            
            CardOverlayView()
        }
        .onAppear {
            cameraService.session.startRunning()
        }
    }
}
