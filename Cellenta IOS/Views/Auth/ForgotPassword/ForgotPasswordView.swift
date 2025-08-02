//
//  ForgotPasswordView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 12.07.2025.
//
import SwiftUI

// MARK: - Custom TextField Style
struct CustomTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(.vertical, 12)
            .padding(.horizontal, 15)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(Color.gray.opacity(0.4), lineWidth: 1)
            )
            .cornerRadius(10)
    }
}

struct ForgotPasswordView: View {
    @Environment(\.presentationMode) var presentationMode
    @Environment(\.dismiss) var dismiss

    @State private var email = ""
    @State private var emailValidationMessage: String? = nil
    @State private var showingAlert = false
    @State private var alertTitle = "Doğrulama Hatası"//"Validation Error"
    @State private var navigateToCodeEntry = false

    let customGradientColors: [Color] = [
        Color(red: 102/255, green: 225/255, blue: 192/255),
        Color(red: 0/255, green: 104/255, blue: 174/255)
    ]

    private var validationIcon: (name: String, color: Color) {
        if email.isEmpty {
            return ("exclamationmark.circle.fill", .gray)
        } else if isValidEmail(email) {
            return ("checkmark.circle.fill", .green)
        } else {
            return ("exclamationmark.circle.fill", .red)
        }
    }

    private func isValidEmail(_ email: String) -> Bool {
        let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        let emailPred = NSPredicate(format: "SELF MATCHES %@", emailRegEx)
        return emailPred.evaluate(with: email)
    }

    private func sendRecoveryEmail() async {
        guard let url = URL(string: "http://34.123.86.69/api/v1/auth/forgot-password") else {
            emailValidationMessage = "Geçersiz sunucu adresi."//"Invalid server URL."
            showingAlert = true
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("IOS", forHTTPHeaderField: "Device-Type")

        let body = ["email": email]
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: body, options: [])

            let (data, response) = try await URLSession.shared.data(for: request)
            let httpResponse = response as? HTTPURLResponse
            let responseBody = String(data: data, encoding: .utf8) ?? "No response body"

            print("📡 Status Code: \(httpResponse?.statusCode ?? -1)")
            print("📦 Response Body: \(responseBody)")

            if httpResponse?.statusCode == 200 {
                navigateToCodeEntry = true
            } else {
                emailValidationMessage = "Kurtarma e-postası gönderilemedi. Lütfen e-posta adresinizi kontrol edin veya daha sonra tekrar deneyin."//"We couldn’t send the recovery email. Please check your email or try again later."
                showingAlert = true
            }
        } catch {
            emailValidationMessage = "Ağ Hatası: \(error.localizedDescription)"//"Network error:
            showingAlert = true
        }
    }

    var body: some View {
        VStack(spacing: 25) {
            // Back Button
            HStack {
                Button {
                    if #available(iOS 15.0, *) {
                        dismiss()
                    } else {
                        presentationMode.wrappedValue.dismiss()
                    }
                } label: {
                    Image(systemName: "arrow.left")
                        .font(.title2)
                        .foregroundColor(customGradientColors[1])
                    Text("Giriş Sayfasına Geri Dön")//"Back to Login"
                        .foregroundColor(customGradientColors[1])
                }
                Spacer()
            }
            .padding(.horizontal)
            .padding(.top, 10)

            // Logo
            VStack(spacing: 10) {
                Image("icon")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 80, height: 80)

                Image("title")
                    .resizable()
                    .scaledToFit()
                    .frame(height: 50)
            }
            .padding(.bottom, 50)

            Text("Şifremi Unuttum")//"Forgot password?"
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundStyle(LinearGradient(gradient: Gradient(colors: customGradientColors), startPoint: .topLeading, endPoint: .bottomTrailing))

            Text("Şifre sıfırlama bağlantısı almak için, aşağıya kayıtlı e-posta adresinizi girin.")//"Enter your registered email address below to receive a password reset link."
                .font(.subheadline)
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            // Email field with icon
            HStack {
                TextField("E-posta Adresi", text: $email)//"Email Address"
                    .textFieldStyle(CustomTextFieldStyle())
                    .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
                    .disableAutocorrection(true)
                    .onChange(of: email) { _, _ in
                        emailValidationMessage = nil
                    }

                Image(systemName: validationIcon.name)
                    .foregroundColor(validationIcon.color)
                    .font(.title2)
                    .padding(.trailing, 10)
            }
            .padding(.horizontal)

            if let message = emailValidationMessage {
                Text(message)
                    .foregroundColor(.red)
                    .font(.caption)
                    .padding(.horizontal)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }

            // Hidden NavigationLink to code entry
            NavigationLink(
                destination: PasswordForgotCodeEntryView(email: email),
                isActive: $navigateToCodeEntry
            ) {
                EmptyView()
            }

            Button(action: {
                if email.isEmpty {
                    emailValidationMessage = "E-posta alanı boş bırakılamaz."//"Email field cannot be empty."
                    showingAlert = true
                } else if !isValidEmail(email) {
                    emailValidationMessage = "E-posta formatı yanlış."//"Email format is incorrect."
                    showingAlert = true
                } else {
                    Task {
                        await sendRecoveryEmail()
                    }
                }
            }) {
                Text("Kurtarma E-postası Gönder")//"Send Recovery Email"
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 15)
                    .background(
                        LinearGradient(gradient: Gradient(colors: customGradientColors), startPoint: .topLeading, endPoint: .bottomTrailing)
                    )
                    .cornerRadius(10)
                    .shadow(color: .black.opacity(0.2), radius: 5, x: 0, y: 5)
            }
            .padding(.horizontal)
            .alert(alertTitle, isPresented: $showingAlert) {
                Button("Tamam") {}//Tamam
            } message: {
                Text(emailValidationMessage ?? "Bir şeyler ters gitti.")//"Something went wrong."
            }

            Spacer()
        }
        .padding()
        .navigationBarHidden(true)
        .navigationTitle("")
    }
}

// MARK: - Preview
struct ForgotPasswordView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ForgotPasswordView()
        }
    }
}
