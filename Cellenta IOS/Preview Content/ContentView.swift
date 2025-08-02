//
//  ContentView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 12.07.2025.
//

 import SwiftUI
 struct ContentView: View {
 @StateObject private var navigationState = NavigationState()
 
 var body: some View {
 // iOS 16+ (NavigationStack)
 if #available(iOS 16, *) {
 NavigationStack(path: $navigationState.path) {
 OpeningView()
 .navigationDestination(for: String.self) { view in
 if view == "Login" { Login() }
 else if view == "SignUp" { SignUpView() }
 }
 }
 }
 // iOS 15 fallback (NavigationView)
 else {
 NavigationView {
 OpeningView()
 .environmentObject(navigationState)
 }
 .navigationViewStyle(.stack)
 }
 }
 }
 
 #Preview {
 ContentView()
 }
 
