//
//  PasswordChangedView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 16.07.2025.
//
import SwiftUI


struct PasswordChangedView: View {
    @Environment(\.presentationMode) var presentationMode // For iOS 14 and earlier
    @Environment(\.dismiss) var dismiss // For iOS 15 and later

    // NEW: State for navigation back to Login
    @State private var navigateToLogin = false

    let customGradientColors: [Color] = [
        Color(red: 102/255, green: 225/255, blue: 192/255),
        Color(red: 0/255, green: 104/255, blue: 174/255)
    ]

    var body: some View {
        VStack(spacing: 25) {
            // No back button needed on this screen based on design
            Spacer()

            // Logo
            VStack(spacing: 10) {
                Image("icon") // Ensure 'icon' asset exists
                    .resizable()
                    .scaledToFit()
                    .frame(width: 80, height: 80)
                    .foregroundColor(customGradientColors[0])

                Image("title") // Ensure 'title' asset exists
                    .resizable()
                    .scaledToFit()
                    .frame(height: 50)
            }
            .padding(.bottom, 50)

            Text("Şifre başarıyla değiştirildi!")//"Password changed successfully!"
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundStyle(LinearGradient(gradient: Gradient(colors: customGradientColors), startPoint: .topLeading, endPoint: .bottomTrailing))
                .multilineTextAlignment(.center)

            Text("Şifreniz başarıyla değiştirildi.")//"Your password has been changed successfully"
                .font(.subheadline)
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            Button(action: {
                // Trigger navigation back to LoginView
                self.navigateToLogin = true
            }) {
                Text("Giriş Yap")//"Back to login"
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 15)
                    .background(
                        LinearGradient(gradient: Gradient(colors: customGradientColors), startPoint: .topLeading, endPoint: .bottomTrailing)
                    )
                    .cornerRadius(10)
                    .shadow(color: Color.black.opacity(0.2), radius: 5, x: 0, y: 5)
            }
            .padding(.horizontal)

            Spacer()

            // NEW: Hidden NavigationLink to go back to LoginView
            // This allows us to push LoginView on top, effectively "resetting" the stack visually
            .background(
                NavigationLink(destination: Login(), isActive: $navigateToLogin) { EmptyView() }
                    .hidden() // Make the NavigationLink itself hidden
            )
        }
        .padding()
        .navigationBarHidden(true) // Hide navigation bar
        .navigationBarBackButtonHidden(true) // Hide default back button
        .navigationTitle("") // Clear any default title
    }
}

struct PasswordChangedView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView { // Crucial for navigation to work
            PasswordChangedView()
        }
    }
}
