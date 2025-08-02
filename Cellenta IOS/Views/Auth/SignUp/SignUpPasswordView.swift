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

    @State private var packages: [Package] = []
    @State private var selectedPackage: Package?
    @State private var isLoadingPackages = true

    let customGradientColors = [
        Color(red: 102/255, green: 225/255, blue: 192/255),
        Color(red: 0/255, green: 104/255, blue: 174/255)
    ]

    var body: some View {
        VStack(spacing: 20) {
            HStack {
                Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        HStack {
                            Image(systemName: "chevron.left")
                            Text("Geri Git") // "Go Back"
                        }
                        .foregroundColor(.blue)
                        .font(.body)
                    }
                    Spacer()
                Text("Hesap Oluştur")//Sign up
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
                    packageSelectionSection
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
            Alert(title: Text("Hesap Oluştur"), message: Text(alertMessage), dismissButton: .default(Text("OK")))//Sign Up, OK
        }
        .navigationDestination(isPresented: $navigateToLogin) {
            Login()
        }
        .task {
            do {
                packages = try await PackageService.fetchPackages()
                selectedPackage = packages.first
            } catch {
                print("Paket Yüklenme Hatası: \(error)")//"Failed to load packages:
            }
            isLoadingPackages = false
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
                            TextField("En az 8 karakter", text: $password)//"At least 8 characters"
                        } else {
                            SecureField("En az 8 karakter", text: $password)//"At least 8 characters"
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
                    Text("Şifre en az 8 karakter olmalıdır.")//"Password must be at least 8 characters"
                        .font(.caption)
                        .foregroundColor(.red)
                }
                else {
                        if !password.isEmpty && !password.contains(where: { $0.isUppercase }) {
                            Text("Şifre en az bir büyük harf içermelidir.") // "Password must contain at least one uppercase letter"
                                .font(.caption)
                                .foregroundColor(.red)
                        }
                        if !password.isEmpty && !password.contains(where: { $0.isLowercase }) {
                            Text("Şifre en az bir küçük harf içermelidir.") // "Password must contain at least one lowercase letter"
                                .font(.caption)
                                .foregroundColor(.red)
                        }
                        if !password.isEmpty && password.range(of: "[0-9]", options: .regularExpression) == nil {
                            Text("Şifre en az bir rakam içermelidir.") // "Password must contain at least one number"
                                .font(.caption)
                                .foregroundColor(.red)
                        }
                        if !password.isEmpty && password.range(of: "[^A-Za-z0-9]", options: .regularExpression) == nil {
                            Text("Şifre en az bir sembol içermelidir.") // "Password must contain at least one symbol"
                                .font(.caption)
                                .foregroundColor(.red)
                        }
                    }
            }

            VStack(alignment: .leading, spacing: 8) {
                Text("Şifreyi onayla")//"Confirm Password"
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                HStack {
                    Group {
                        if isConfirmPasswordVisible {
                            TextField("Şifreyi tekrar girin", text: $confirmPassword)//"Repeat your password"
                        } else {
                            SecureField("Şifreyi tekrar girin", text: $confirmPassword)//"Repeat your password"
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
                    Text("Şifreler eşleşmiyor")//"Passwords don't match"
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }
        }
    }
    
    private var packageSelectionSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Paket seçiniz")//"Select a Package"
                .font(.subheadline)
                .foregroundColor(.secondary)

            if isLoadingPackages {
                ProgressView("Paketler yükleniyor...")//"Loading packages..."
                    .padding(.top, 4)
            } else if packages.isEmpty {
                Text("Şu anda mevcut paket yok.")//"No packages available at the moment."
                    .foregroundColor(.gray)
                    .padding(.top, 4)
            } else {
                ForEach(packages.indices, id: \.self) { index in
                    let pkg = packages[index]

                    VStack(alignment: .leading, spacing: 10) {
                        // Header
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(pkg.packageName)
                                    .font(.headline)
                                Text("\(pkg.amountMinutes) dk | \(pkg.amountSms) SMS | \(pkg.amountData / 1000) GB")//min, SMS, MB
                                    .font(.subheadline)
                                    .foregroundColor(.gray)
                            }
                            Spacer()
                            Image(systemName: selectedPackage?.package_id == pkg.package_id ? "checkmark.circle.fill" : "circle")
                                .foregroundColor(selectedPackage?.package_id == pkg.package_id ? .green : .gray)
                        }

                        // Details if selected
                        if selectedPackage?.package_id == pkg.package_id {
                            VStack(alignment: .leading, spacing: 10) {
                                HStack(spacing: 16) {
                                    balanceItem(
                                        icon: "wifi",
                                        value: "\(pkg.amountData / 1000)",
                                        label: "GB",
                                        color: .blue
                                    )//Data
                                    balanceItem(icon: "phone", value: "\(pkg.amountMinutes)", label: "Dk", color: .green)//Min
                                    balanceItem(icon: "message", value: "\(pkg.amountSms)", label: "SMS", color: .orange)
                                    balanceItem(icon: "clock", value: "\(pkg.period)", label: "Gün", color: .purple)//Days
                                }

                                Text("Fiyat: \(String(format: "%.2f TL", pkg.price))")//Price:
                                    .font(.subheadline)
                                    .fontWeight(.medium)
                                    .foregroundColor(.black)
                            }
                            .padding(.top, 4)
                        }
                    }
                    .padding()
                    .background(Color.white)
                    .cornerRadius(12)
                    .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(selectedPackage?.package_id == pkg.package_id ? Color.green : Color.gray.opacity(0.3), lineWidth: 1)
                    )
                    .onTapGesture {
                        withAnimation {
                            selectedPackage = pkg
                        }
                    }
                    .padding(.vertical, 4)
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
                Text(isSigningUp ? "Hesap Oluşturuluyor..." : "Kayıt Ol")//"Creating Account..." : "Sign Up"
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
            Text("Hesabınız var mı?")//"Already have an account?"
                .font(.subheadline)
                .foregroundColor(.secondary)

            Button(action: {
                navigateToLogin = true
            }) {
                Text("Giriş yap")//"Log in"
                    .font(.subheadline)
                    .bold()
                    .foregroundColor(Color(red: 0/255, green: 104/255, blue: 174/255))
            }
        }
        .padding(.vertical)
    }

    private func validatePasswords() -> Bool {
        if password.isEmpty {
            alertMessage = "Lütfen şifrenizi oluşturun"//"Please create a password"
            showAlert = true
            return false
        }

        if !isValidPassword(password) {
            alertMessage = "Şifreniz en az 8 karakterden oluşturulmalıdır ve içerisinde:\n- 1 büyük harf\n- 1 küçük harf\n- 1 rakam\n- 1 özel karakter (!,@#$...)"//"Password must be at least 8 characters and include:\n- 1 uppercase letter\n- 1 lowercase letter\n- 1 number\n- 1 special character (!@#$...)"
            showAlert = true
            return false
        }

        if confirmPassword.isEmpty {
            alertMessage = "Lütfen şifrenizi doğrulayın"//"Please confirm your password"
            showAlert = true
            return false
        }

        if password != confirmPassword {
            alertMessage = "Şifreler eşleşmiyor"//"Passwords don't match"
            showAlert = true
            return false
        }

        return true
    }

    private func sendSignUpRequest() {
        guard let url = URL(string: "http://34.123.86.69/api/v1/auth/register") else {
            alertMessage = "Geçersiz Kayıt URL'si"//"Invalid signup URL"
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
            alertMessage = "Kayıt başarısız oldu"//"Failed to encode signup data"
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
                    alertMessage = "Ağ Hatası: \(error.localizedDescription)"//"Network error:
                    showAlert = true
                }
                return
            }

            guard let httpResponse = response as? HTTPURLResponse else {
                DispatchQueue.main.async {
                    alertMessage = "Geçersiz Sunucu Cevabı"//"Invalid server response"
                    showAlert = true
                }
                return
            }

            if (200...299).contains(httpResponse.statusCode), let data = data {
                do {
                    let signUpResponse = try JSONDecoder().decode(SignUpResponse.self, from: data)

                    DispatchQueue.main.async {
                        alertMessage = "Hesap Başarıyla Oluşturuldu!"//"Account created successfully!"
                        showAlert = true

                        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                            completeSignUp(from: signUpResponse)
                        }
                    }
                } catch {
                    DispatchQueue.main.async {
                        alertMessage = "⚠️ Hesap Oluşturuldu, Ancak Yanıt Kodu Başarısız Oldu"//"⚠️ Account created, but failed to decode response"
                        showAlert = true
                        //completeSignUp(from: signUpResponse)
                    }
                }
            } else {
                var errorMessage = "Başarısız Kayıt"
                if let data = data {
                    let raw = String(data: data, encoding: .utf8) ?? ""
                    errorMessage = translateBackendError(raw)
                }

                DispatchQueue.main.async {
                    alertMessage = errorMessage
                    showAlert = true
                }
            }
        }.resume()
    }

    private func completeSignUp(from response: SignUpResponse) {
        // Store in memory
        UserSession.shared.name = response.name
        UserSession.shared.surname = response.surname
        UserSession.shared.msisdn = response.msisdn
        UserSession.shared.email = response.email
        UserSession.shared.password = password

        // Store persistently
        UserDefaults.standard.set(response.msisdn, forKey: "msisdn")
        UserDefaults.standard.set(response.cust_id, forKey: "customerId")

        // Activate selected package if any
        if let selectedPackage = selectedPackage {
            Task {
                do {
                    let balance = try await PackageService.selectPackage(
                        customerId: response.cust_id,
                        packageId: selectedPackage.package_id,
                        msisdn: response.msisdn
                    )
                    print("✅ Paket \(selectedPackage.packageName) başarıyla aktifleştirildi!")//"✅ Package \(selectedPackage.packageName) activated successfully!"
                    print("📦 Balance: \(balance.remainingData)MB, \(balance.remainingMinutes)dk, \(balance.remainingSms)SMS")//min
                } catch {
                    print("❌ Paket etkinleştirilemedi: \(error.localizedDescription)")//"❌ Package activation failed:
                }
            }
        }

        // Go to HomeView
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

func isValidPassword(_ password: String) -> Bool {
    let passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\[\\]{};':\"\\\\|,.<>/?-]).{8,}$"
    return NSPredicate(format: "SELF MATCHES %@", passwordRegex).evaluate(with: password)
}

private func balanceItem(icon: String, value: String, label: String, color: Color) -> some View {
    VStack(spacing: 4) {
        Image(systemName: icon)
            .foregroundColor(color)
            .font(.caption)
        Text(value)
            .font(.caption)
            .fontWeight(.bold)
        Text(label)
            .font(.system(size: 10))
            .foregroundColor(.gray)
    }
    .frame(maxWidth: .infinity)
}

private func translateBackendError(_ rawError: String) -> String {
    let lower = rawError.lowercased()

    if lower.contains("user already exists") {
        return "Bu kullanıcı zaten mevcut."
    } else if lower.contains("invalid email") {
        return "Geçersiz e-posta adresi."
    } else if lower.contains("msisdn already exists") {
        return "Bu telefon numarası zaten kullanılıyor."
    } else if lower.contains("email already exists") {
        return "Bu e-posta adresi zaten kayıtlı."
    } else if lower.contains("missing") || lower.contains("required") {
        return "Lütfen tüm gerekli alanları doldurun."
    }

    return "Bir hata oluştu: \(rawError)"
}
