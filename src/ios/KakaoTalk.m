#import "KakaoTalk.h"
#import <Cordova/CDVPlugin.h>
#import <KakaoOpenSDK/KakaoOpenSDK.h>

@implementation KakaoTalk

- (void) login:(CDVInvokedUrlCommand*) command
{
    [[KOSession sharedSession] close];
	[[KOSession sharedSession] openWithCompletionHandler:^(NSError *error) {
	    if ([[KOSession sharedSession] isOpen]) {
	        // login success
	        NSLog(@"login succeeded.");
            [KOSessionTask userMeTaskWithCompletion:^(NSError *error, KOUserMe *me) {
                CDVPluginResult* pluginResult = nil;
                if (me) {
                    // success
                    NSLog(@"userId=%@", me.ID);
                    NSLog(@"email=%@", me.account.email);
                    NSLog(@"nickName=%@", me.nickname);
                    NSLog(@"profileImage=%@", me.profileImageURL);
                    NSLog(@"accessToken=%@", [KOSession sharedSession].token.accessToken);
                    
                    NSMutableDictionary *userSession = [@{} mutableCopy];
                    userSession[@"id"] = me.ID;
                    userSession[@"nickname"] = me.nickname;
                    userSession[@"access_token"] = [KOSession sharedSession].token.accessToken;
                    
                    // Profile Image 가 null 로 넘어오는 경우가 있어서, null check.
                    NSString *profileImage = [me.profileImageURL absoluteString];
                    if(profileImage && profileImage.length > 0) {
                        userSession[@"profileImage"] = profileImage;
                    }
                    
                    NSString *email = me.account.email;
                    if(email && email.length > 0) {
                        userSession[@"email"] = email;
                    }
                    
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:userSession];
                } else {
                    // failed
                    NSLog(@"login session failed.");
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
                }
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }];
	    } else {
	        // failed
	        NSLog(@"login failed.");
	        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	    }
	    
	    
	}];
}

- (void)logout:(CDVInvokedUrlCommand*)command
{
	[[KOSession sharedSession] logoutAndCloseWithCompletionHandler:^(BOOL success, NSError *error) {
	    if (success) {
	        // logout success.
            NSLog(@"Successful logout.");
	    } else {
	        // failed
	        NSLog(@"failed to logout.");
	    }
	}];
	CDVPluginResult* pluginResult = pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)loginCallback:(CDVInvokedUrlCommand*)command
{
    NSString *urlString = [NSString stringWithFormat:@"%@", [command.arguments objectAtIndex:0]];
    NSURL *url = [NSURL URLWithString:[urlString stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLHostAllowedCharacterSet]]];
    
    if ([KOSession isKakaoAccountLoginCallback:url]) {
        [KOSession handleOpenURL:url];
    }

    CDVPluginResult* pluginResult = pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
