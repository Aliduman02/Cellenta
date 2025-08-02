//
//  ConversionExtension.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 24.07.2025.
//
import Foundation

extension Double {
    func rounded(toPlaces places: Int) -> Double {
        let multiplier = pow(10.0, Double(places))
        return (self * multiplier).rounded() / multiplier
    }
}
