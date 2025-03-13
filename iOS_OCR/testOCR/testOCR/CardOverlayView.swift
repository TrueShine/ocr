//
//  CardOverlayView.swift
//  testOCR
//
//  Created by JinukRyu on 3/10/25.
//

import SwiftUI

struct CardOverlayView: View {
    var showRedBox: Bool = true
    
    // 공통 비율
    private let cardWidthRatio: CGFloat = 0.85
    private let cardAspectRatio: CGFloat = 0.63 // 신용카드 비율
    
    var body: some View {
        GeometryReader { geometry in
            let width = geometry.size.width * cardWidthRatio
            let height = width * cardAspectRatio
            let centerX = geometry.size.width / 2
            let centerY = geometry.size.height / 2
            
            ZStack {
                // 딤 처리 with 투명 박스
                Color.black.opacity(0.6)
                    .mask(
                        Rectangle()
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .frame(width: width, height: height)
                                    .position(x: centerX, y: centerY)
                                    .blendMode(.destinationOut)
                            )
                    )
                    .compositingGroup()
                
                // 흰색 카드 테두리
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.white, lineWidth: 2)
                    .frame(width: width, height: height)
                    .position(x: centerX, y: centerY)
                
                // 빨간 박스 (OCR 영역 - 정확히 일치)
                if showRedBox {
                    RoundedRectangle(cornerRadius: 4)
                        .stroke(Color.red, lineWidth: 2)
                        .frame(width: width, height: height)
                        .position(x: centerX, y: centerY)
                }
            }
        }
        .ignoresSafeArea()
        .allowsHitTesting(false)
    }
}
