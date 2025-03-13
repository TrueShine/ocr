import SwiftUI

struct CameraView: View {
    @StateObject private var cameraService = CameraService()
    @State private var selectedMode: OCRMode = .card {
        didSet {
            cameraService.ocrMode = selectedMode == .card ? .card : .license
        }
    }
    
    enum OCRMode: String, CaseIterable, Identifiable {
        case card = "카드"
        case license = "운전면허증"
        
        var id: String { rawValue }
    }
    
    var body: some View {
        ZStack(alignment: .top) {
            CameraPreview(session: cameraService.session)
                .ignoresSafeArea()
            CardOverlayView()
            
            VStack(spacing: 12) {
                Picker("모드 선택", selection: $selectedMode) {
                    ForEach(OCRMode.allCases) { mode in
                        Text(mode.rawValue).tag(mode)
                    }
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()
                .background(Color.black.opacity(0.5))
                .cornerRadius(8)
                .padding(.top, 40)
                .padding(.horizontal)
                .zIndex(1)
                
                if selectedMode == .card {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("카드번호: \(cameraService.cardNumber)")
                        Text("유효기간: \(cameraService.expiryDate)")
                    }
                    .padding()
                    .background(Color.black.opacity(0.6))
                    .foregroundColor(.white)
                    .cornerRadius(10)
                    .font(.system(size: 14, weight: .medium, design: .monospaced))
                    .padding(.horizontal)
                } else {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("운전면허번호: \(cameraService.cardNumber)")
                        Text("갱신만료일: \(cameraService.expiryDate)")
                        Text("생년월일: \(cameraService.birthDate)")
                        Text("주민번호 앞자리: \(cameraService.regNumberPrefix)")
                        Text("면허 발급일: \(cameraService.licenseIssueDate)")
                        Text("일련번호: \(cameraService.licenseSerial)")
                    }
                    .padding()
                    .background(Color.black.opacity(0.6))
                    .foregroundColor(.white)
                    .cornerRadius(10)
                    .font(.system(size: 14, weight: .medium, design: .monospaced))
                    .padding(.horizontal)
                }
                
                Spacer()
            }
            
            VStack {
                Spacer()
                
                ScrollView {
                    VStack(alignment: .leading, spacing: 4) {
                        ForEach(cameraService.filteredRecognizedText.components(separatedBy: "\n"), id: \.self) { line in
                            Text(line)
                                .font(.system(size: 12, design: .monospaced))
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
                }
                .frame(maxWidth: .infinity, maxHeight: 200)
                .background(Color.black.opacity(0.6))
                .foregroundColor(.white)
                .cornerRadius(10)
                .padding(.horizontal)
                .padding(.bottom, 20)
            }
            
            
        }
        .onAppear {
            DispatchQueue.global(qos: .userInitiated).async {
                cameraService.session.startRunning()
            }
        }
        .onDisappear {
            cameraService.session.stopRunning()
        }
    }
}
