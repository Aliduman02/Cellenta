//
//  NavigationState.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 16.07.2025.
//

import SwiftUI
import Combine  // For ObservableObject (if needed)

class NavigationState: ObservableObject {
    // For iOS 16+ (NavigationStack)
    @Published var path = NavigationPath()
    
    // For iOS 15 and below (NavigationView)
    @Published var shouldPopToRoot = false

    func popToRoot() {
        if #available(iOS 16, *) {
            path.removeLast(path.count)  // iOS 16+ method
        } else {
            shouldPopToRoot = true  // Fallback for iOS 15
        }
    }
}
