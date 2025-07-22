//
//  SignUpPasswordView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 16.07.2025.
//

struct SignUpResponse: Codable {
    let cust_id: Int
    let msisdn: String
    let name: String
    let surname: String
    let email: String
    let sdate: String
}

import SwiftUI

struct SignUpPasswordView: View {
    let signUpData: SignUpData
    @Environment(\.presentationMode) var presentationMode
    @Environment(\.dismiss) var dismiss

    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var isPasswordVisible = false
    @State private var isConfirmPasswordVisible = false
    @State private var showAlert = false
    @State private var alertMessage = ""
    @State private var isSigningUp = false
    @State private var navigateToLogin = false

    let customGradientColors = [
        Color(red: 102/255, green: 225/255, blue: 192/255),
        Color(red: 0/255, green: 104/255, blue: 174/255)
    ]

    var body: some View {
        VStack(spacing: 20) {
            HStack {
                Text("Sign up")
                    .font(.largeTitle)
                    .bold()
                    .foregroundColor(Color(red: 46/255, green: 163/255, blue: 155/255))
                    .padding(.leading)
                Spacer()
                Image("icon")
                    .resizable()
                    .frame(width: 60, height: 60)
                    .padding(.trailing)
            }
            .padding(.top, 20)

            ScrollView {
                VStack(spacing: 25) {
                    passwordFieldSection
                    signUpButton
                    alreadyHaveAccountSection
                }
                .padding(.horizontal)
                .padding(.top, 10)
            }
        }
        .background(Color(.systemGroupedBackground).ignoresSafeArea())
        .navigationBarHidden(true)
        .navigationBarBackButtonHidden(true)
        .alert(isPresented: $showAlert) {
            Alert(title: Text("Sign Up"), message: Text(alertMessage), dismissButton: .default(Text("OK")))
        }
        .navigationDestination(isPresented: $navigateToLogin) {
            Login()
        }
    }

    private var passwordFieldSection: some View {
        Group {
            VStack(alignment: .leading, spacing: 8) {
                Text("Create Password")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                HStack {
                    Group {
                        if isPasswordVisible {
                            TextField("At least 8 characters", text: $password)
                        } else {
                            SecureField("At least 8 characters", text: $password)
                        }
                    }
                    .textContentType(.newPassword)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
                    Button(action: { isPasswordVisible.toggle() }) {
                        Image(systemName: isPasswordVisible ? "eye.slash.fill" : "eye.fill")
                            .foregroundColor(.gray)
                    }
                }
                .padding()
                .background(Color(.systemBackground))
                .cornerRadius(10)
                .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)

                if !password.isEmpty && password.count < 8 {
                    Text("Password must be at least 8 characters")
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }

            VStack(alignment: .leading, spacing: 8) {
                Text("Confirm Password")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                HStack {
                    Group {
                        if isConfirmPasswordVisible {
                            TextField("Repeat your password", text: $confirmPassword)
                        } else {
                            SecureField("Repeat your password", text: $confirmPassword)
                        }
                    }
                    .textContentType(.newPassword)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
                    Button(action: { isConfirmPasswordVisible.toggle() }) {
                        Image(systemName: isConfirmPasswordVisible ? "eye.slash.fill" : "eye.fill")
                            .foregroundColor(.gray)
                    }
                }
                .padding()
                .background(Color(.systemBackground))
                .cornerRadius(10)
                .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)

                if !confirmPassword.isEmpty && password != confirmPassword {
                    Text("Passwords don't match")
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }
        }
    }

    private var signUpButton: some View {
        Button(action: {
            if validatePasswords() {
                isSigningUp = true
                sendSignUpRequest()
            }
        }) {
            HStack {
                if isSigningUp {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                }
                Text(isSigningUp ? "Creating Account..." : "Sign Up")
            }
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .frame(height: 50)
            .background(
                LinearGradient(
                    gradient: Gradient(colors: customGradientColors),
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .cornerRadius(10)
            .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 5)
        }
        .disabled(isSigningUp)
    }

    private var alreadyHaveAccountSection: some View {
        HStack {
            Text("Already have an account?")
                .font(.subheadline)
                .foregroundColor(.secondary)

            Button(action: {
                navigateToLogin = true
            }) {
                Text("Log in")
                    .font(.subheadline)
                    .bold()
                    .foregroundColor(Color(red: 0/255, green: 104/255, blue: 174/255))
            }
        }
        .padding(.vertical)
    }

    private func validatePasswords() -> Bool {
        if password.isEmpty {
            alertMessage = "Please create a password"
            showAlert = true
            return false
        }

        if !isValidPassword(password) {
            alertMessage = """
            Password must be at least 8 characters and include:
            - 1 uppercase letter
            - 1 lowercase letter
            - 1 number
            - 1 special character (!@#$...)
            """
            showAlert = true
            return false
        }

        if confirmPassword.isEmpty {
            alertMessage = "Please confirm your password"
            showAlert = true
            return false
        }

        if password != confirmPassword {
            alertMessage = "Passwords don't match"
            showAlert = true
            return false
        }

        return true
    }

    private func sendSignUpRequest() {
        guard let url = URL(string: "http://34.123.86.69/api/v1/auth/register") else {
            alertMessage = "Invalid signup URL"
            showAlert = true
            isSigningUp = false
            return
        }

        let requestBody: [String: Any] = [
            "msisdn": signUpData.phoneNumber,
            "password": password,
            "name": signUpData.firstName,
            "surname": signUpData.lastName,
            "email": signUpData.email
        ]

        guard let jsonData = try? JSONSerialization.data(withJSONObject: requestBody) else {
            alertMessage = "Failed to encode signup data"
            showAlert = true
            isSigningUp = false
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = jsonData

        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                isSigningUp = false
            }

            if let error = error {
                DispatchQueue.main.async {
                    alertMessage = "Network error: \(error.localizedDescription)"
                    showAlert = true
                }
                return
            }

            guard let httpResponse = response as? HTTPURLResponse else {
                DispatchQueue.main.async {
                    alertMessage = "Invalid server response"
                    showAlert = true
                }
                return
            }

            if (200...299).contains(httpResponse.statusCode) {
                DispatchQueue.main.async {
                    alertMessage = "Account created successfully!"
                    showAlert = true
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                        completeSignUp()
                    }
                }
            } else {
                var errorMessage = "Sign-up failed"
                if let data = data,
                   let json = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any],
                   let message = json["message"] as? String {
                    errorMessage = message
                } else if let raw = data.flatMap({ String(data: $0, encoding: .utf8) }) {
                    errorMessage = "Server error: \(raw)"
                }

                DispatchQueue.main.async {
                    alertMessage = errorMessage
                    showAlert = true
                }
            }
        }.resume()
    }

    private func completeSignUp() {
        // Set the shared session values
        UserSession.shared.name = "\(signUpData.firstName)"
        UserSession.shared.surname = signUpData.lastName
        UserSession.shared.msisdn = signUpData.phoneNumber
        UserSession.shared.email = signUpData.email
        UserSession.shared.password = password
        
        //Newly added
        UserDefaults.standard.set(signUpData.phoneNumber, forKey: "msisdn")
        // If your register API returns cust_id, parse it and save it below
        UserDefaults.standard.set(0, forKey: "customerId") // ← TEMP fallback if not returned

        alertMessage = "Account created successfully!"
        showAlert = true

        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let window = windowScene.windows.first {
                let homeView = HomeView()
                if #available(iOS 16.0, *) {
                    window.rootViewController = UIHostingController(
                        rootView: NavigationStack { homeView }
                    )
                } else {
                    window.rootViewController = UIHostingController(
                        rootView: NavigationView { homeView }
                            .navigationViewStyle(.stack)
                    )
                }
                window.makeKeyAndVisible()
            }
        }
    }
}

struct SignUpPasswordView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            SignUpPasswordView(signUpData: SignUpData(firstName: "", lastName: "", phoneNumber: "", email: ""))
        }
    }
}
func isValidPassword(_ password: String) -> Bool {
    let passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\[\\]{};':\"\\\\|,.<>/?-]).{8,}$"
    return NSPredicate(format: "SELF MATCHES %@", passwordRegex).evaluate(with: password)
}
