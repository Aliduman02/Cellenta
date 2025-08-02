//
//  SignUpView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 12.07.2025.
//
import SwiftUI

struct SignUpView: View {
    @Environment(\.presentationMode) var presentationMode
    @Environment(\.dismiss) var dismiss

    @State private var firstName = ""
    @State private var lastName = ""
    @State private var phoneNumber = ""
    @State private var email = ""

    @State private var showAlert = false
    @State private var alertMessage = ""
    @State private var navigateToPasswordSetup = false
    
    // Track validation states
    @State private var isFirstNameValid = false
    @State private var isLastNameValid = false
    @State private var isPhoneValid = false
    @State private var isEmailValid = false
    
    var body: some View {
        VStack(spacing: 20) {
            Spacer().frame(height: 20)

            // Title & Logo
            HStack {
                Text("Sign up")
                    .font(.title)
                    .bold()
                    .foregroundColor(Color(red: 46/255, green: 163/255, blue: 155/255))
                Spacer()
                Image("icon")
                    .resizable()
                    .frame(width: 50, height: 50)
            }
            .padding(.horizontal)
            

            // Input fields with validation tracking
            inputField(title: "Ad", text: $firstName, isValid: $isFirstNameValid, validator: isValidName)//First Name
            inputField(title: "Soyad", text: $lastName, isValid: $isLastNameValid, validator: isValidName)//Last Name
            
            inputField(title: "Telefon Numarası", text: $phoneNumber, isValid: $isPhoneValid, validator: isValidPhone, keyboard: .numberPad)//Phone Number
                .onChange(of: phoneNumber) { newValue in
                    // Allow only digits
                    let digitsOnly = newValue.filter { $0.isNumber }
                    
                    // Ensure first digit is 5
                    if digitsOnly.first == "5" {
                        phoneNumber = String(digitsOnly.prefix(10))
                    } else if !digitsOnly.isEmpty {
                        // If first digit is not 5, discard the input
                        phoneNumber = ""
                    }
                }

            inputField(title: "E-posta", text: $email, isValid: $isEmailValid, validator: isValidEmail, keyboard: .emailAddress)//E-Mail

            // Continue button - disabled until all fields are valid
            Button(action: {
                if validateFields() {
                    navigateToPasswordSetup = true
                }
            }) {
                Text("Sonraki Adım")//Continue
                    .foregroundColor(.white)
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
                    .opacity(allFieldsValid ? 1 : 0.6)
            }
            .padding(.horizontal)
            .disabled(!allFieldsValid)
            .background(
                NavigationLink(
                    destination: SignUpPasswordView(signUpData: SignUpData(
                        firstName: firstName,
                        lastName: lastName,
                        phoneNumber: phoneNumber,
                        email: email
                    )),
                    isActive: $navigateToPasswordSetup
                ) {
                    EmptyView()
                }
                .hidden()
            )

            // Log in link
            HStack(spacing: 5) {
                Text("Zaten hesabınız var mı?")//Already have an account?
                    .font(.footnote)
                Button("Giriş yap") {//Log in
                    if #available(iOS 15, *) {
                        dismiss()
                    } else {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
                .font(.footnote)
                .bold()
                .foregroundColor(.black)
            }
            Spacer()
        }
        .alert(isPresented: $showAlert) {
            Alert(title: Text("Doğrulama Hatası"), message: Text(alertMessage), dismissButton: .default(Text("OK")))//"Validation Error"
        }
        .navigationBarHidden(true)
        .navigationBarBackButtonHidden(true)
        .navigationTitle("")
    }

    private var allFieldsValid: Bool {
        isFirstNameValid && isLastNameValid && isPhoneValid && isEmailValid
    }

    func inputField(
        title: String,
        text: Binding<String>,
        isValid: Binding<Bool>,
        validator: @escaping (String) -> Bool,
        keyboard: UIKeyboardType = .default
    ) -> some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(title)
                .font(.footnote)
                .foregroundColor(.black)

            HStack {
                TextField("\(title.lowercased()) giriniz", text: text)//Enter your
                    .keyboardType(keyboard)
                    .padding(.leading)
                    .frame(height: 50)
                    .onChange(of: text.wrappedValue) { newValue in
                        if title.contains("Ad") {//Ad
                            let allowedCharacters = CharacterSet(charactersIn: "abcdefghijklmnopqrstuvwxyzçğıöşüABCDEFGHIJKLMNOPQRSTUVWXYZÇĞİÖŞÜ ")
                            let filtered = newValue.prefix(60).filter {
                                String($0).rangeOfCharacter(from: allowedCharacters) != nil
                            }
                            text.wrappedValue = String(filtered)
                        } else if title == "E-posta" {//E-Mail
                            text.wrappedValue = newValue.lowercased()
                        }

                        isValid.wrappedValue = validator(text.wrappedValue)
                    }

                let showCheckmark = isValid.wrappedValue && !text.wrappedValue.isEmpty
                let showExclamation = !isValid.wrappedValue && !text.wrappedValue.isEmpty

                Image(systemName: showCheckmark ? "checkmark.circle.fill" : "exclamationmark.circle.fill")
                    .foregroundColor(
                        showCheckmark ? .green :
                        (showExclamation ? .red : .gray)
                    )
                    .padding(.trailing)
            }
            .background(Color.white)
            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.gray.opacity(0.3)))
        }
        .padding(.horizontal)
        .onAppear {
            isValid.wrappedValue = validator(text.wrappedValue)
        }
    }

    func isValidName(_ name: String) -> Bool {
        let allowedCharacters = CharacterSet(charactersIn: "abcdefghijklmnopqrstuvwxyzçğıöşüABCDEFGHIJKLMNOPQRSTUVWXYZÇĞİÖŞÜ ")
        return !name.isEmpty &&
               name.rangeOfCharacter(from: allowedCharacters.inverted) == nil &&
               name.count <= 60
    }

    func isValidPhone(_ number: String) -> Bool {
        let digitsOnly = CharacterSet.decimalDigits
        return number.count == 10 && CharacterSet(charactersIn: number).isSubset(of: digitsOnly)
    }

    func isValidEmail(_ email: String) -> Bool {
        if email.isEmpty { return false }
        let emailFormat = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        let emailPredicate = NSPredicate(format: "SELF MATCHES %@", emailFormat)
        return emailPredicate.evaluate(with: email)
    }

    func validateFields() -> Bool {
        guard !firstName.isEmpty else {
            alertMessage = "Lütfen adınızı girin."//"Please enter your first name."
            showAlert = true
            return false
        }
        guard isValidName(firstName) else {
            alertMessage = "Adınız sadece harf ve boşluk içermelidir."//"First name can contain only letters and spaces."
            showAlert = true
            return false
        }
        guard !lastName.isEmpty else {
            alertMessage = "Lütfen soyadınızı girin."//"Please enter your last name."
            showAlert = true
            return false
        }
        guard isValidName(lastName) else {
            alertMessage = "Soyadınız sadece harf ve boşluk içermelidir."//"Last name can contain only letters and spaces."
            showAlert = true
            return false
        }
        guard !phoneNumber.isEmpty else {
            alertMessage = "Lütfen telefon numaranızı girin."//"Please enter your phone number."
            showAlert = true
            return false
        }
        guard isValidPhone(phoneNumber) else {
            alertMessage = "Telefon numarası 10 haneli olmalıdır."//"Phone number must be exactly 10 digits."
            showAlert = true
            return false
        }
        guard !email.isEmpty else {
            alertMessage = "Lütfen e-posta adresinizi girin."//"Please enter your email."
            showAlert = true
            return false
        }
        guard isValidEmail(email) else {
            alertMessage = "E-posta formatı yanlış"//"Email format is incorrect."
            showAlert = true
            return false
        }
        return true
    }
}

struct SignUpView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            SignUpView()
        }
    }
}
