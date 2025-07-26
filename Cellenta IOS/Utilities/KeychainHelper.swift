//
//  KeychainHelper.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 18.07.2025.
//
import Foundation
import Security

class KeychainManager {
    static func save(_ data: Data, service: String, account: String) -> Bool {
        let query: [String: Any] = [
            kSecClass as String       : kSecClassGenericPassword,
            kSecAttrService as String : service,
            kSecAttrAccount as String : account,
            kSecValueData as String   : data
        ]
        
        SecItemDelete(query as CFDictionary) // Remove any existing item
        let status = SecItemAdd(query as CFDictionary, nil)
        return status == errSecSuccess
    }
    
    static func read(service: String, account: String) -> Data? {
        let query: [String: Any] = [
            kSecClass as String       : kSecClassGenericPassword,
            kSecAttrService as String : service,
            kSecAttrAccount as String : account,
            kSecReturnData as String  : kCFBooleanTrue!,
            kSecMatchLimit as String  : kSecMatchLimitOne
        ]
        
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        
        if status == errSecSuccess {
            return result as? Data
        }
        return nil
    }
    
    static func delete(service: String, account: String) {
        let query: [String: Any] = [
            kSecClass as String       : kSecClassGenericPassword,
            kSecAttrService as String : service,
            kSecAttrAccount as String : account
        ]
        SecItemDelete(query as CFDictionary)
    }
}
