//
//  PasswordForgotCodeEntryView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 16.07.2025.
//
import SwiftUI

struct PasswordForgotCodeEntryView: View {
    @Environment(\.presentationMode) var presentationMode
    @Environment(\.dismiss) var dismiss

    let email: String

    @State private var code: String = ""
    @FocusState private var isTextFieldFocused: Bool

    @State private var remainingTime: Int = 120
    @State private var timer: Timer? = nil
    @State private var showAlert = false
    @State private var alertMessage = ""
    @State private var didTimeout = false
    @State private var navigateToResetPassword = false

    let customGradientColors: [Color] = [
        Color(red: 102/255, green: 225/255, blue: 192/255),
        Color(red: 0/255, green: 104/255, blue: 174/255)
    ]

    var body: some View {
        VStack(spacing: 25) {
            topBar
            logoAndTitle
            instructionTexts
            codeInput
            verifyActions
            resendSection
            Spacer()
        }
        .padding()
        .navigationBarHidden(true)
        .navigationBarBackButtonHidden(true)
        .navigationTitle("")
        .alert("Code Verification", isPresented: $showAlert) {
            Button("OK") {
                if alertMessage == "Code verified successfully!" {
                    navigateToResetPassword = true
                }
            }
        } message: {
            Text(alertMessage)
        }
        .navigationDestination(isPresented: $navigateToResetPassword) {
            ResetPassword(email: email, verificationCode: code)
        }
    }
}

// MARK: - Subviews
private extension PasswordForgotCodeEntryView {
    var topBar: some View {
        HStack {
            Button {
                if #available(iOS 15, *) {
                    dismiss()
                } else {
                    presentationMode.wrappedValue.dismiss()
                }
            } label: {
                Image(systemName: "arrow.left")
                Text("Back")
            }
            .font(.headline)
            .foregroundColor(Color(red: 0, green: 104/255, blue: 174/255))
            Spacer()
        }
        .padding(.horizontal)
        .padding(.top, 10)
    }

    var logoAndTitle: some View {
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
        .padding(.bottom, 50)
    }

    var instructionTexts: some View {
        VStack(spacing: 5) {
            Text("Please check your email")
                .font(.title)
                .fontWeight(.bold)
                .foregroundStyle(LinearGradient(gradient: Gradient(colors: customGradientColors), startPoint: .topLeading, endPoint: .bottomTrailing))

            Text("We've sent a code to \(email)")
                .font(.subheadline)
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
        }
    }

    var codeInput: some View {
        HStack(spacing: 10) {
            ForEach(0..<6) { index in
                Text(getCharacter(at: index))
                    .font(.title2)
                    .frame(width: 45, height: 55)
                    .background(Color.white)
                    .cornerRadius(10)
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(shouldHighlight(index: index) ? Color.blue : Color.gray.opacity(0.4), lineWidth: 2)
                    )
            }
        }
        .padding(.horizontal)
        .background(
            TextField("", text: $code)
                .keyboardType(.numberPad)
                .font(.largeTitle)
                .opacity(0.01)
                .focused($isTextFieldFocused)
                .onChange(of: code) { _, newVal in
                    code = String(newVal.prefix(6).filter { $0.isNumber })
                }
        )
        .onTapGesture {
            isTextFieldFocused = true
        }
        .onAppear {
            isTextFieldFocused = true
            startTimer()
        }
        .onDisappear {
            timer?.invalidate()
        }
    }

    var verifyActions: some View {
        VStack(spacing: 10) {
            Button("Clear Code") {
                code = ""
                isTextFieldFocused = true
            }
            .foregroundColor(.teal)

            Button("Verify") {
                verifyCode()
            }
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 15)
            .background(LinearGradient(gradient: Gradient(colors: customGradientColors), startPoint: .topLeading, endPoint: .bottomTrailing))
            .cornerRadius(10)
            .padding(.horizontal)
        }
    }

    var resendSection: some View {
        HStack {
            Button("Send code again") {
                sendCodeAgain()
            }
            .disabled(remainingTime > 0)
            .foregroundColor(remainingTime == 0 ? .blue : .gray)

            Text(String(format: "%02d:%02d", remainingTime / 60, remainingTime % 60))
                .foregroundColor(.gray)
        }
    }
}

// MARK: - Logic
private extension PasswordForgotCodeEntryView {
    func startTimer() {
        timer?.invalidate()
        remainingTime = 120
        didTimeout = false
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
            if remainingTime > 0 {
                remainingTime -= 1
            } else {
                timer?.invalidate()
                alertMessage = "Request timed out. Please click send code again."
                showAlert = true
                didTimeout = true
            }
        }
    }

    func sendCodeAgain() {
        Task {
            do {
                try await AuthService.shared.sendRecoveryEmail(email: email)
                alertMessage = "A new verification code was sent to your email."
                showAlert = true
                startTimer() // Optional: restart the timer
            } catch {
                alertMessage = "Failed to resend code: \(error.localizedDescription)"
                showAlert = true
            }
        }
    }

    func verifyCode() {
        guard !didTimeout else {
            alertMessage = "The request has timed out. Please send a new code."
            showAlert = true
            navigateToResetPassword = true
            print("✅ Code verified response: \(code)")
            return
        }

        guard code.count == 6 else {
            alertMessage = "Please enter a valid 6-digit code."
            showAlert = true
            return
        }

        Task {
            do {
                let success = try await AuthService.shared.verifyResetCode(email: email, code: code)

                if success {
                    alertMessage = "Code verified successfully!"
                    timer?.invalidate()
                    showAlert = true
                } else {
                    alertMessage = "Invalid code. Please try again."
                    showAlert = true
                }
            } catch {
                alertMessage = "Verification failed: \(error.localizedDescription)"
                showAlert = true
            }
        }
    }

    func getCharacter(at index: Int) -> String {
        if index < code.count {
            let char = code[code.index(code.startIndex, offsetBy: index)]
            return String(char)
        }
        return ""
    }

    func shouldHighlight(index: Int) -> Bool {
        return code.count == index && isTextFieldFocused
    }
}
