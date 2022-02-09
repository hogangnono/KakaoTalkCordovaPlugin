// KakaoTalkCordovaPlugin - v.1.0.0
// (c) 2015 Lihak Kang, lihak@hotmail.com, MIT Licensed.
// KakaoTalk.h may be freely distributed under the MIT license.
//
//  KakaoTalk.h
//

#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>

@interface KakaoTalk : CDVPlugin

- (void) login:(CDVInvokedUrlCommand*)command;
- (void) logout:(CDVInvokedUrlCommand*)command;
- (void) loginCallback:(CDVInvokedUrlCommand*)command;
- (void) isAvailable:(CDVInvokedUrlCommand*)command;

@end
