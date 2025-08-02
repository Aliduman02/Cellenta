//
//  ResetPassword.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 16.07.2025.
//
import SwiftUI

struct ResetPassword: View {
    @Environment(\.presentationMode) var presentationMode
    @Environment(\.dismiss) var dismiss

    let email: String
    let verificationCode: String

    @State private var newPassword = ""
    @State private var confirmNewPassword = ""
    @State private var showNewPassword = false
    @State private var showConfirmNewPassword = false
    @State private var showAlert = false
    @State private var alertMessage = ""
    @State private var navigateToPasswordChanged = false
    @State private var navigateToLogin = false

    let customGradientColors: [Color] = [
        Color(red: 102/255, green: 225/255, blue: 192/255),
        Color(red: 0/255, green: 104/255, blue: 174/255)
    ]

    private func isPasswordValid(_ password: String) -> Bool {
        let pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\[\\]{};':\"\\\\|,.<>/?-]).{8,}$"
        return NSPredicate(format: "SELF MATCHES %@", pattern).evaluate(with: password)
    }

    private func resetPassword() {
        guard isPasswordValid(newPassword) else {
            alertMessage = """
            Şifre en az 8 karakter uzunluğunda olmalı ve şunları içermelidir:
                -    1 büyük harf
                -    1 küçük harf
                -    1 rakam
                -    1 özel karakter (!@#$…)
            """
            showAlert = true
            return
        }

        guard newPassword == confirmNewPassword else {
            alertMessage = "Şifreler eşleşmiyor."
            showAlert = true
            return
        }

        Task {
            do {
                let success = try await AuthService.shared.changePassword(
                    email: email,
                    password: newPassword,
                    verificationCode: verificationCode
                )

                await MainActor.run {
                    if success {
                        // ✅ Update stored password only when actually changed
                        UserDefaults.standard.set(newPassword, forKey: "userPassword")
                        UserSession.shared.password = newPassword

                        alertMessage = "Şifreniz başarıyla sıfırlandı!"
                        showAlert = true

                        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                            navigateToPasswordChanged = true
                        }
                    } else {
                        alertMessage = "Şifre sıfırlama başarısız oldu. Lütfen daha sonra tekrar deneyin."
                        showAlert = true
                    }
                }
            } catch {
                await MainActor.run {
                    alertMessage = "Error: \(error.localizedDescription)"
                    showAlert = true
                }
            }
        }
    }

    var body: some View {
        VStack(spacing: 25) {
            // Back Button
            HStack {
                Button(action: {
                    if #available(iOS 15, *) {
                        dismiss()
                    } else {
                        presentationMode.wrappedValue.dismiss()
                    }
                }) {
                    Image(systemName: "arrow.left")
                        .font(.title2)
                        .foregroundColor(customGradientColors[1])
                    Text("Geri")//Back
                        .font(.headline)
                        .foregroundColor(customGradientColors[1])
                }
                Spacer()
            }
            .padding(.horizontal)
            .padding(.top, 10)

            // Logo
            HStack(spacing: 10) {
                Image("icon")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 50, height: 50)

                Image("title")
                    .resizable()
                    .scaledToFit()
                    .frame(height: 50)
            }
            .padding(.top, 30)
            .padding(.bottom, 50)

            Text("Şifre Sıfırlama")//Reset Password
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundStyle(LinearGradient(
                    gradient: Gradient(colors: customGradientColors),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                ))
                .multilineTextAlignment(.center)

            Text("Lütfen hatırlayabileceğiniz bir şifre yazın.")//"Please type something you'll remember"
                .font(.subheadline)
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            // New Password
            VStack(alignment: .leading, spacing: 5) {
                HStack {
                    Group {
                        if showNewPassword {
                            TextField("En az 8 karakter olmalı ve bir rakam, bir büyük harf, bir küçük harf ve bir sembol içermelidir.", text: $newPassword)
                            //Must be at least 8 characters and include a number, an uppercase letter, a lowercase letter and a symbol.
                        } else {
                            SecureField("En az 8 karakter olmalı ve bir sayı, bir büyük harf, bir küçük harf ve bir sembol içermelidir.", text: $newPassword)
                            //Must be at least 8 characters and include a number, an uppercase letter, a lowercase letter and a symbol.
                        }
                    }
                    .textFieldStyle(CustomTextFieldStyle())
                    .textInputAutocapitalization(.never)
                    .disableAutocorrection(true)

                    Button(action: {
                        showNewPassword.toggle()
                    }) {
                        Image(systemName: showNewPassword ? "eye.slash.fill" : "eye.fill")
                            .foregroundColor(.gray)
                    }
                }
                .padding(.horizontal)

                if !newPassword.isEmpty && !isPasswordValid(newPassword) {
                    Text("""
                         Şifre en az 8 karakter uzunluğunda olmalı ve şunları içermelidir:
                             -    1 büyük harf
                             -    1 küçük harf
                             -    1 rakam
                             -    1 özel karakter (!@#$…))
                         """)
                        .font(.caption)
                        .foregroundColor(.red)
                        .padding(.horizontal)
                    /*Password must be at least 8 characters and include:
                     - 1 uppercase letter
                     - 1 lowercase letter
                     - 1 digit
                     - 1 special character (!@#$...)*/
                }
            }

            // Confirm Password
            VStack(alignment: .leading, spacing: 5) {
                HStack {
                    Group {
                        if showConfirmNewPassword {
                            TextField("Şifreyi tekrar girin", text: $confirmNewPassword)//Repeat password
                        } else {
                            SecureField("Şifreyi tekrar girin", text: $confirmNewPassword)//Repeat password
                        }
                    }
                    .textFieldStyle(CustomTextFieldStyle())
                    .textInputAutocapitalization(.never)
                    .disableAutocorrection(true)

                    Button(action: {
                        showConfirmNewPassword.toggle()
                    }) {
                        Image(systemName: showConfirmNewPassword ? "eye.slash.fill" : "eye.fill")
                            .foregroundColor(.gray)
                    }
                }
                .padding(.horizontal)

                if !confirmNewPassword.isEmpty && newPassword != confirmNewPassword {
                    Text("Şifreler eşleşmiyor.")//"Passwords do not match."
                        .font(.caption)
                        .foregroundColor(.red)
                        .padding(.horizontal)
                }
            }

            // Submit Button
            Button(action: resetPassword) {
                Text("Şifreyi sıfırla")//"Reset password"
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 15)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: customGradientColors),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .cornerRadius(10)
                    .shadow(color: .black.opacity(0.2), radius: 5, x: 0, y: 5)
            }
            .padding(.horizontal)

            Spacer()

            // Already have account
            HStack {
                Text("Zaten hesabınız var mı?")//"Already have an account?"
                    .font(.subheadline)
                    .foregroundColor(.gray)

                Button("Giriş yap") {//Log in
                    navigateToLogin = true
                }
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundColor(customGradientColors[1])
            }
            .padding(.bottom, 20)
        }
        .padding()
        .navigationBarHidden(true)
        .navigationTitle("")
        .alert("Şifre Sıfırlama Başarılı!", isPresented: $showAlert) {//Password Reset
            Button("OK") {}
        } message: {
            Text(alertMessage)
        }
        .navigationDestination(isPresented: $navigateToPasswordChanged) {
            PasswordChangedView()
        }
        .navigationDestination(isPresented: $navigateToLogin) {
            Login()
        }
    }
}

// MARK: - Preview
struct ResetPasswordView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ResetPassword(email: "test@example.com", verificationCode: "123456")
        }
    }
}
