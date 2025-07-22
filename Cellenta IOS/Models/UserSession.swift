//
//  UserSession.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 19.07.2025.
//
import Foundation

class UserSession: ObservableObject {
    static let shared = UserSession()

    @Published var name: String = ""
    @Published var surname: String = ""
    @Published var msisdn: String = ""
    @Published var email: String = ""
    @Published var password: String = ""

    private init() {}
}
