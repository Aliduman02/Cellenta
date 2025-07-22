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

    private let gradientColors = [
        Color(red: 102/255, green: 225/255, blue: 192/255),
        Color(red: 0/255, green: 104/255, blue: 174/255)
    ]

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

            Text("Log in")
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
                    Text("Remember Me").font(.subheadline)
                }
                .toggleStyle(SwitchToggleStyle(tint: .black))
                .labelsHidden()

                Text("Remember Me").font(.subheadline)
                Spacer()

                NavigationLink(destination: ForgotPasswordView()) {
                    Text("Forgot your password?")
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
                    Text("Log in")
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
                Text("Don't have an account?")
                    .font(.footnote)
                NavigationLink(destination: SignUpView()) {
                    Text("Sign up")
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
                title: Text("Login Error"),
                message: Text(alertMessage),
                dismissButton: .default(Text("OK"))
            )
        }
        .navigationDestination(isPresented: $isLoggedIn) {
            HomeView()
        }
    }

    // MARK: - Components

    private var phoneNumberField: some View {
        HStack {
            TextField("Phone Number (5XXXXXXXX)", text: $phoneNumber)
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
                    TextField("Password", text: $password)
                } else {
                    SecureField("Password", text: $password)
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

    /*private func isValidPassword(_ password: String) -> Bool {
        let passwordRegex = "^(?=.*[A-Z])(?=.*[a-z]).{8,}$"
        return password.range(of: passwordRegex, options: .regularExpression) != nil
    }*/

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

                    if rememberMe {
                        KeychainHelper.save(formattedPhone, forKey: "savedPhoneNumber")
                    }

                    isLoggedIn = true
                }
            } catch {
                await MainActor.run {
                    alertMessage = "Login failed. Please check your phone number and password and try again." // ← Generic error message
                    showAlert = true
                }
            }

            await MainActor.run {
                isLoading = false
            }
        }
    }
    private func validateInputs() -> Bool {
        guard !phoneNumber.isEmpty else {
            alertMessage = "Please enter your phone number"
            showAlert = true
            return false
        }

        guard isValidPhoneNumber(phoneNumber) else {
            alertMessage = "Please enter a valid 10-digit phone number"
            showAlert = true
            return false
        }

        guard !password.isEmpty else {
            alertMessage = "Please enter your password"
            showAlert = true
            return false
        }

        /*guard isValidPassword(password) else {
            alertMessage = "Password must be at least 8 characters with one uppercase and one lowercase letter"
            showAlert = true
            return false
        }*/

        return true
    }

    private func extractErrorMessage(from error: Error) -> String {
        if let urlError = error as? URLError {
            switch urlError.code {
            case .notConnectedToInternet, .networkConnectionLost:
                return "No internet connection."
            case .timedOut:
                return "Connection timed out."
            default:
                return "Network error. Please try again."
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
