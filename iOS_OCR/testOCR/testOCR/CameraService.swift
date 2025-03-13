import AVFoundation
import SwiftUI
import Vision
import CoreImage

class CameraService: NSObject, ObservableObject {
    enum OCRMode {
        case card
        case license
    }
    
    var session = AVCaptureSession()
    private let output = AVCaptureVideoDataOutput()
    private let recognizer = VisionTextRecognizer()
    
    @Published var recognizedText: String = "" {
        didSet {
            let filtered = ocrMode == .license ? filterLicenseText(recognizedText) : recognizedText
            self.filteredRecognizedText = filtered
            
            switch ocrMode {
            case .card:
                let result = extractCardInfo(from: recognizedText)
                self.cardNumber = result.number ?? "인식 안됨"
                self.expiryDate = result.expiry ?? "인식 안됨"
            case .license:
                let info = extractLicenseInfo(from: recognizedText)
                self.cardNumber = info["운전면허번호"] ?? "-"
                self.expiryDate = info["갱신만료일"] ?? "-"
                self.birthDate = info["생년월일"] ?? "-"
                self.regNumberPrefix = info["주민번호 앞자리"] ?? "-"
                self.licenseIssueDate = info["면허 발급일"] ?? "-"
                self.licenseSerial = info["일련번호"] ?? "-"
            }
        }
    }
    
    @Published var filteredRecognizedText: String = ""
    @Published var cardNumber: String = ""
    @Published var expiryDate: String = ""
    @Published var birthDate: String = ""
    @Published var regNumberPrefix: String = ""
    @Published var licenseIssueDate: String = ""
    @Published var licenseSerial: String = ""
    
    var ocrMode: OCRMode = .card
    
    private let cardCropRect = CGRect(x: 0.075, y: 0.28375, width: 0.85, height: 0.5355)
    
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
                if granted { self.setupSession() }
            }
        default: break
        }
    }
    
    private func setupSession() {
        session.beginConfiguration()
        session.sessionPreset = .high
        
        guard let device = AVCaptureDevice.default(for: .video),
              let input = try? AVCaptureDeviceInput(device: device),
              session.canAddInput(input) else {
            return
        }
        
        session.addInput(input)
        
        output.setSampleBufferDelegate(self, queue: DispatchQueue(label: "camera.frame.queue"))
        output.alwaysDiscardsLateVideoFrames = true
        
        if session.canAddOutput(output) {
            session.addOutput(output)
        }
        
        if let connection = output.connection(with: .video), connection.isVideoOrientationSupported {
            connection.videoOrientation = .portrait
        }
        
        session.commitConfiguration()
    }
    
    private func extractCardInfo(from text: String) -> (number: String?, expiry: String?) {
        var cardNumber: String?
        var expiryDate: String?
        
        let cleanText = text.replacingOccurrences(of: "\n", with: " ")
        let cardRegex = try! NSRegularExpression(pattern: #"(\\d{4}[\\s\\-]*\\d{4}[\\s\\-]*\\d{4}[\\s\\-]*\\d{4})"#)
        if let match = cardRegex.firstMatch(in: cleanText, range: NSRange(cleanText.startIndex..., in: cleanText)) {
            if let range = Range(match.range, in: cleanText) {
                let raw = cleanText[range]
                let normalized = raw.replacingOccurrences(of: #"[^0-9]"#, with: " ", options: .regularExpression)
                cardNumber = normalized.replacingOccurrences(of: #" +"#, with: " ", options: .regularExpression).trimmingCharacters(in: .whitespaces)
            }
        }
        
        let expiryRegex = try! NSRegularExpression(pattern: #"(0[1-9]|1[0-2])/([0-9]{2})"#)
        if let match = expiryRegex.firstMatch(in: cleanText, range: NSRange(cleanText.startIndex..., in: cleanText)) {
            if let range = Range(match.range, in: cleanText) {
                expiryDate = String(cleanText[range])
            }
        }
        
        return (cardNumber, expiryDate)
    }
    
    private func extractLicenseInfo(from text: String) -> [String: String] {
        var result: [String: String] = [:]
        let cleanedText = text
            .replacingOccurrences(of: "\n", with: " ")
            .replacingOccurrences(of: #"\s+"#, with: " ", options: .regularExpression)
        
        if let match = cleanedText.range(of: #"\b\d{2}-\d{2}-\d{6}-\d{2}\b"#, options: .regularExpression) {
            result["운전면허번호"] = String(cleanedText[match])
        }
        
        if let match = cleanedText.range(of: #"\b\d{6}\b"#, options: .regularExpression) {
            let value = String(cleanedText[match])
            result["생년월일"] = value
            result["주민번호 앞자리"] = value
        }
        
        if let match = cleanedText.range(of: #"\d{4}\.\d{2}\.31"#, options: .regularExpression) {
            result["갱신만료일"] = String(cleanedText[match])
        }
        
        if let match = cleanedText.range(of: #"\d{4}\.\d{2}\.\d{2}"#, options: .regularExpression) {
            result["면허 발급일"] = String(cleanedText[match])
        }
        
        if let match = cleanedText.range(of: #"[A-Z0-9]{6}"#, options: .regularExpression) {
            result["일련번호"] = String(cleanedText[match])
        }
        
        return result
    }
    
    private func filterLicenseText(_ text: String) -> String {
        let lines = text.components(separatedBy: CharacterSet.newlines.union([","]))
        
        let filtered = lines.compactMap { line -> String? in
            let trimmed = line.trimmingCharacters(in: .whitespacesAndNewlines)
            
            // 한글 제거
            let noHangul = trimmed.replacingOccurrences(of: "[가-힣]", with: "", options: .regularExpression)
            
            // 허용 문자만 유지
            let cleaned = noHangul.replacingOccurrences(of: #"[^A-Za-z0-9.\-]"#, with: "", options: .regularExpression)
            
            return cleaned.isEmpty ? nil : cleaned
        }
        
        return filtered.joined(separator: "\n")
    }
}

extension CameraService: AVCaptureVideoDataOutputSampleBufferDelegate {
    func captureOutput(_ output: AVCaptureOutput,
                       didOutput sampleBuffer: CMSampleBuffer,
                       from connection: AVCaptureConnection) {
        recognizer.recognize(from: sampleBuffer, cropRect: cardCropRect) { [weak self] text in
            DispatchQueue.main.async {
                self?.recognizedText = text
            }
        }
    }
}
