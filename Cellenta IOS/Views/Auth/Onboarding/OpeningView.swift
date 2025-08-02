//
//  OpeningView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 16.07.2025.
//

import SwiftUI

struct OpeningView: View {
    var body: some View {
        VStack(spacing: 30) { // Increased spacing for better visual separation

            Spacer() // Pushes content to the center vertically

            // Logo Image
            // Ensure "icon" image asset is in your project's Assets.xcassets
            Image("icon")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 150, height: 150) // Adjust size as needed

            // Title Image
            // Ensure "title" image asset (for "CELLENTA") is in your project's Assets.xcassets
            Image("title")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 200, height: 50) // Adjust size as needed
                .padding(.bottom, 50) // Add some space below the title

            // Sign In Button (Navigates to LoginView)
            // WRAP THE BUTTON IN NavigationLink
            
            //NavigationLink(destination: Login()) { // Destination is Login()
            NavigationLink(destination: Login().id(UUID())) {
                Text("Giriş Yap")//Sign in
                    .foregroundColor(.white)
                    .font(.headline)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color(red: 102/255, green: 225/255, blue: 192/255),
                                Color(red: 0/255, green: 104/255, blue: 174/255)
                            ]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .cornerRadius(12)
            }
            .padding(.horizontal) // Add horizontal padding to the NavigationLink

            // Create Account Button (Navigates to SignUpView)
            // WRAP THE BUTTON IN NavigationLink
            NavigationLink(destination: SignUpView()) { // Destination is SignupView()
                Text("Hesap Oluştur")//Create account
                    .foregroundColor(Color(red: 0/255, green: 104/255, blue: 174/255)) // Set text color to match the gradient end color
                    .font(.headline)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color.white) // White background for "Create account"
                    .cornerRadius(12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color(red: 0/255, green: 104/255, blue: 174/255), lineWidth: 1) // Blue border
                    )
            }
            .padding(.horizontal) // Add horizontal padding to the NavigationLink

            Spacer() // Pushes content to the center vertically
        }
        .navigationBarHidden(true) // Hide the navigation bar if you don't need it on the opening screen
        .navigationBarBackButtonHidden(true) // Hide the back button on the opening screen itself
    }
}

struct OpeningView_Previews: PreviewProvider {
    static var previews: some View {
        // For preview, wrap in NavigationView if you want to see navigation bar effects
        NavigationView { // Keep this for preview to simulate navigation context
            OpeningView()
        }
    }
}
