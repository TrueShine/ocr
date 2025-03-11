//
//  CardOverlayView.swift
//  testOCR
//
//  Created by JinukRyu on 3/10/25.
//

import SwiftUI

struct CardOverlayView: View {
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // 전체 딤 처리
                Color.black.opacity(0.6)
                    .mask(
                        Rectangle()
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .frame(
                                        width: geometry.size.width * 0.85,
                                        height: geometry.size.width * 0.85 * 0.63
                                    )
                                    .position(x: geometry.size.width / 2, y: geometry.size.height / 2)
                                    .blendMode(.destinationOut)
                            )
                    )
                    .compositingGroup()
                
                // 테두리
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.white, lineWidth: 2)
                    .frame(
                        width: geometry.size.width * 0.85,
                        height: geometry.size.width * 0.85 * 0.63
                    )
                    .position(x: geometry.size.width / 2, y: geometry.size.height / 2)
            }
        }
        .allowsHitTesting(false)
    }
}
