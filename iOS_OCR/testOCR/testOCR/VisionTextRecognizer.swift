import Vision
import AVFoundation
import CoreImage

class VisionTextRecognizer {
    func recognize(from sampleBuffer: CMSampleBuffer,
                   cropRect: CGRect,
                   completion: @escaping (String) -> Void) {
        
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else {
            completion("이미지 버퍼 없음")
            return
        }
        
        let ciImage = CIImage(cvPixelBuffer: pixelBuffer)
        let width = CGFloat(CVPixelBufferGetWidth(pixelBuffer))
        let height = CGFloat(CVPixelBufferGetHeight(pixelBuffer))
        
        let cropRectInPixels = CGRect(
            x: cropRect.origin.x * width,
            y: (1 - cropRect.origin.y - cropRect.height) * height,
            width: cropRect.width * width,
            height: cropRect.height * height
        )
        
        let cropped = ciImage.cropped(to: cropRectInPixels)
        
        let request = VNRecognizeTextRequest { request, error in
            guard error == nil,
                  let results = request.results as? [VNRecognizedTextObservation] else {
                completion("인식 실패: \(error?.localizedDescription ?? "알 수 없음")")
                return
            }
            
            let text = results.compactMap { $0.topCandidates(1).first?.string }.joined(separator: "\n")
            DispatchQueue.main.async {
                completion(text)
            }
        }
        
        request.recognitionLevel = .accurate
        request.usesLanguageCorrection = true
        request.recognitionLanguages = ["ko-KR", "en-US"]
        
        let handler = VNImageRequestHandler(ciImage: cropped, orientation: .right, options: [:])
        
        DispatchQueue.global(qos: .userInitiated).async {
            do {
                try handler.perform([request])
            } catch {
                DispatchQueue.main.async {
                    completion("Vision 요청 실패: \(error.localizedDescription)")
                }
            }
        }
    }
}
