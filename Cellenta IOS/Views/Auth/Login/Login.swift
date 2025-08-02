//
//  Login.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 12.07.2025.
//

import SwiftUI

struct Login: View {
    @State private var phoneNumber = ""
    @State private var password = ""
    @State private var rememberMe = false
    @State private var isPasswordVisible = false
    @State private var showAlert = false
    @State private var alertMessage = ""
    @State private var isLoggedIn = false
    @State private var isLoading = false
    @State private var autoLogin = false


    private let gradientColors = [
        Color(red: 102/255, green: 225/255, blue: 192/255),
        Color(red: 0/255, green: 104/255, blue: 174/255)
    ]

    init() {
        // Load saved credentials if they exist
        let savedPhone = KeychainHelper.read(forKey: "savedPhoneNumber")
        let savedPassword = KeychainHelper.read(forKey: "savedPassword")
        
        _phoneNumber = State(initialValue: savedPhone ?? "")
        _password = State(initialValue: savedPassword ?? "")
        _rememberMe = State(initialValue: savedPhone != nil && savedPassword != nil)
    }

    var body: some View {
        VStack(spacing: 20) {
            Spacer().frame(height: 10)

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

            Text("Giriş Yap")
                .font(.title)
                .bold()
                .foregroundStyle(LinearGradient(
                    gradient: Gradient(colors: gradientColors),
                    startPoint: .leading,
                    endPoint: .trailing
                ))

            phoneNumberField
            passwordField

            HStack {
                Toggle(isOn: $rememberMe) {
                    Text("Beni Hatırla").font(.subheadline) //Remember me
                }
                .toggleStyle(SwitchToggleStyle(tint: .black))
                .labelsHidden()

                Text("Beni Hatırla").font(.subheadline)//Remember me
                Spacer()

                NavigationLink(destination: ForgotPasswordView()) {
                    Text("Şifremi Unuttum")//Forgot password
                        .font(.subheadline)
                        .foregroundColor(.black)
                }
            }
            .padding(.horizontal)

            Button(action: loginUser) {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text("Giriş Yap")//Log in
                        .fontWeight(.semibold)
                }
            }
            .disabled(isLoading)
            .frame(maxWidth: .infinity)
            .frame(height: 50)
            .foregroundColor(.white)
            .background(LinearGradient(
                gradient: Gradient(colors: gradientColors),
                startPoint: .leading,
                endPoint: .trailing
            ))
            .cornerRadius(12)
            .padding(.horizontal)

            HStack(spacing: 5) {
                Text("Hesabınız yok mu?")//"Don't have an account?"
                    .font(.footnote)
                NavigationLink(destination: SignUpView()) {
                    Text("Hesap Oluştur")//Sign Up
                        .font(.footnote)
                        .bold()
                        .foregroundColor(.black)
                }
            }
            .padding(.top, 10)

            Spacer()
        }
        .background(Color(.systemGroupedBackground).ignoresSafeArea())
        .navigationBarHidden(true)
        .navigationBarBackButtonHidden(true)
        .alert(isPresented: $showAlert) {
            Alert(
                title: Text("Giriş Hatalı"),//Login Error
                message: Text(alertMessage),
                dismissButton: .default(Text("Tamam"))//OK
            )
        }
        .navigationDestination(isPresented: $isLoggedIn) {
            HomeView()
        }
        .onAppear {
            let savedPhone = KeychainHelper.read(forKey: "savedPhoneNumber")
            let savedPassword = KeychainHelper.read(forKey: "savedPassword")
            
            phoneNumber = savedPhone ?? ""
            password = savedPassword ?? ""
            rememberMe = savedPhone != nil && savedPassword != nil
            
            // 👇 Auto-login only if both exist and not already logged in
            if let savedPhone, let savedPassword, !isLoggedIn {
                Task {
                    await loginUserAuto(phone: savedPhone, pass: savedPassword)
                }
            }
        }
    }

    // MARK: - Components

    private var phoneNumberField: some View {
        HStack {
            TextField("Telefon Numarası (5XXXXXXXX)", text: $phoneNumber)//"Phone Number (5XXXXXXXX)"
                .keyboardType(.numberPad)
                .padding(.leading)
                .frame(height: 50)
                .onChange(of: phoneNumber) { newValue in
                    let filtered = newValue.filter { $0.isNumber }
                    phoneNumber = String(filtered.prefix(10))
                }

            Image(systemName: phoneValidationIcon)
                .foregroundColor(phoneValidationColor)
                .padding(.trailing)
        }
        .background(Color.white)
        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.gray.opacity(0.3)))
        .padding(.horizontal)
    }

    private var passwordField: some View {
        HStack {
            Group {
                if isPasswordVisible {
                    TextField("Şifre", text: $password)//Password
                } else {
                    SecureField("Şifre", text: $password)//Password
                }
            }
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled(true)
            .padding(.leading)
            .frame(height: 50)

            Button(action: { isPasswordVisible.toggle() }) {
                Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                    .foregroundColor(.gray)
                    .padding(.trailing)
            }
        }
        .background(Color.white)
        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.gray.opacity(0.3)))
        .padding(.horizontal)
    }

    // MARK: - Validation

    private var phoneValidationIcon: String {
        if phoneNumber.isEmpty {
            return "exclamationmark.circle.fill"
        } else if !isValidPhoneNumber(phoneNumber) {
            return "exclamationmark.circle.fill"
        } else {
            return "checkmark.circle.fill"
        }
    }

    private var phoneValidationColor: Color {
        if phoneNumber.isEmpty {
            return .gray
        } else if !isValidPhoneNumber(phoneNumber) {
            return .red
        } else {
            return .green
        }
    }

    private func isValidPhoneNumber(_ number: String) -> Bool {
        let phoneRegex = "^\\d{10}$"
        return number.range(of: phoneRegex, options: .regularExpression) != nil
    }

    // MARK: - Login Logic

    private func loginUser() {
        guard validateInputs() else { return }

        Task {
            isLoading = true
            let formattedPhone = phoneNumber

            do {
                let response = try await AuthService.shared.login(msisdn: formattedPhone, password: password)

                // Store user info globally
                await MainActor.run {
                    UserSession.shared.name = response.name
                    UserSession.shared.surname = response.surname
                    UserSession.shared.msisdn = response.msisdn
                    UserSession.shared.email = response.email
                    UserSession.shared.password = password
                    //Newly added
                    UserDefaults.standard.set(response.msisdn, forKey: "msisdn")
                    UserDefaults.standard.set(response.cust_id, forKey: "customerId")

                    // Handle remember me functionality
                    handleRememberMe(phoneNumber: formattedPhone)

                    isLoggedIn = true
                }
            } catch {
                await MainActor.run {
                    alertMessage = "Giriş hatalı! Lütfen telefon numaranızı ve şifrenizi kontrol ediniz."//"Login failed. Please check your phone number and password and try again."
                    showAlert = true
                }
            }

            await MainActor.run {
                isLoading = false
            }
        }
    }
    
    private func loginUserAuto(phone: String, pass: String) async {
        await MainActor.run { isLoading = true }

        do {
            let response = try await AuthService.shared.login(msisdn: phone, password: pass)

            await MainActor.run {
                UserSession.shared.name = response.name
                UserSession.shared.surname = response.surname
                UserSession.shared.msisdn = response.msisdn
                UserSession.shared.email = response.email
                UserSession.shared.password = pass
                UserDefaults.standard.set(response.msisdn, forKey: "msisdn")
                UserDefaults.standard.set(response.cust_id, forKey: "customerId")

                isLoggedIn = true // 🔁 Navigate to HomeView
            }
        } catch {
            await MainActor.run {
                alertMessage = "Otomatik giriş hatalı. Lütfen manuel giriş yapınız."//Auto-login failed. Please log in manually.
                showAlert = true
            }
        }

        await MainActor.run { isLoading = false }
    }
    
    private func handleRememberMe(phoneNumber: String) {
        if rememberMe {
            // Save both phone number and password to keychain
            KeychainHelper.save(phoneNumber, forKey: "savedPhoneNumber")
            KeychainHelper.save(password, forKey: "savedPassword")
        } else {
            // Remove both phone number and password from keychain
            KeychainHelper.delete(forKey: "savedPhoneNumber")
            KeychainHelper.delete(forKey: "savedPassword")
        }
    }
    
    private func validateInputs() -> Bool {
        guard !phoneNumber.isEmpty else {
            alertMessage = "Lütfen telefon numaranızı girin"//"Please enter your phone number"
            showAlert = true
            return false
        }

        guard isValidPhoneNumber(phoneNumber) else {
            alertMessage = "Lütfen 10 haneli bir telefon numarası girin"//"Please enter a valid 10-digit phone number"
            showAlert = true
            return false
        }

        guard !password.isEmpty else {
            alertMessage = "Lütfen şifrenizi girin"//"Please enter your password"
            showAlert = true
            return false
        }

        return true
    }

    private func extractErrorMessage(from error: Error) -> String {
        if let urlError = error as? URLError {
            switch urlError.code {
            case .notConnectedToInternet, .networkConnectionLost:
                return "İnternet bağlantınızı kontrol ediniz"//"No internet connection."
            case .timedOut:
                return "Bağlantı zaman aşımına uğradı"//Connection timed out."
            default:
                return "Ağ hatası, lütfen tekrar deneyin"//"Network error. Please try again."
            }
        }

        if let authError = error as? AuthError {
            return authError.message
        }

        return error.localizedDescription
    }
}

#Preview {
    Login()
}
