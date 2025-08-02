//
//  ProfileView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 17.07.2025.
//
import SwiftUI
import AuthenticationServices

struct ProfileView: View {
    @State private var showLogoutAlert = false
    @State private var navigateToOpening = false
    @Binding var selectedTab: Tab
    @AppStorage("isLoggedIn") var isLoggedIn = false

    @ObservedObject var session = UserSession.shared

    var body: some View {
        NavigationView {
            VStack(alignment: .leading) {
                // MARK: - Navigation Bar
                HStack {
                    Spacer()
                    Text("Hesabım")//Profile
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.black)
                    Spacer()
                }
                .padding(.horizontal, 24)
                .padding(.top, 10)

                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        // MARK: - Profile Picture Placeholder with Initial
                        ZStack {
                            Circle()
                                .fill(Color.gray.opacity(0.3))
                                .frame(width: 120, height: 120)

                            Text(String(session.name.prefix(1)))
                                .font(.system(size: 50, weight: .bold))
                                .foregroundColor(.white)
                        }
                        .padding(.top, 30)
                        .padding(.leading, 30)

                        // MARK: - Profile Information Fields
                        ProfileInfoRow(label: "Ad", value: session.name)//Name
                        ProfileInfoRow(label: "Soyad", value: session.surname)//Surname
                        ProfileInfoRow(label: "Telefon Numarası", value: session.msisdn)//Phone Number
                        ProfileInfoRow(label: "E-posta", value: session.email)//Email

                        // MARK: - Logout Button
                        Button(action: {
                            showLogoutAlert = true
                        }) {
                            Text("Çıkış Yap")//Log Out
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(.white)
                                .padding(.vertical, 10)
                                .padding(.horizontal, 40)
                                .background(Color.red)
                                .cornerRadius(10)
                        }
                        .padding(.top, 24)
                        .padding(.leading, 120)
                        .alert(isPresented: $showLogoutAlert) {
                            Alert(
                                title: Text("Çıkış yapmak istediğinizden emin misiniz?"),//"Are you sure you want to log out?"
                                primaryButton: .destructive(Text("Çıkış Yap")) {//Log Out
                                    navigateToOpening = true
                                    clearSession()
                                },
                                secondaryButton: .cancel()
                            )
                        }

                        // Hidden NavigationLink
                        NavigationLink(
                            destination: OpeningView(),
                            isActive: $navigateToOpening,
                            label: { EmptyView() }
                        )

                        Spacer()
                    }
                }
                .background(Color.white.edgesIgnoringSafeArea(.all))
            }
            .navigationBarHidden(true)
        }
    }

    private func clearSession() {
        print("🧹 Clearing session...")
        KeychainHelper.delete(forKey: "savedPhoneNumber")
        KeychainHelper.delete(forKey: "savedPassword")
        UserSession.shared.reset()
        UserDefaults.standard.removeObject(forKey: "msisdn")
        UserDefaults.standard.removeObject(forKey: "customerId")
    }
}

// MARK: - ProfileInfoRow
struct ProfileInfoRow: View {
    let label: String
    let value: String
    var isPassword: Bool = false
    @State private var isVisible: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption)
                .fontWeight(.medium)
                .foregroundColor(.gray)

            HStack {
                Text(isPassword && !isVisible ? String(repeating: "*", count: value.count) : value)
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(.black)
                
                if isPassword {
                    Spacer()
                    Button(action: {
                        isVisible.toggle()
                    }) {
                        Text(isVisible ? "Hide" : "Show")
                            .font(.footnote)
                            .foregroundColor(.blue)
                    }
                }
            }
        }
        .padding(.horizontal, 45)
    }
}
// MARK: - Preview
struct ProfileView_Previews: PreviewProvider {
    @State static var selectedTab: Tab = .profile

    static var previews: some View {
        ProfileView(selectedTab: $selectedTab)
    }
}
