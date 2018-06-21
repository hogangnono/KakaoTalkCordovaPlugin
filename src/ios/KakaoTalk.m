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
	        [KOSessionTask meTaskWithCompletionHandler:^(KOUser* result, NSError *error) {
                CDVPluginResult* pluginResult = nil;
			    if (result) {
			        // success
			        NSLog(@"userId=%@", result.ID);
                    NSLog(@"email=%@", [result propertyForKey:@"email"]);
			        NSLog(@"nickName=%@", [result propertyForKey:@"nickname"]);
                    NSLog(@"profileImage=%@", [result propertyForKey:@"profile_image"]);
                    NSLog(@"accessToken=%@", [KOSession sharedSession].accessToken);
			        
                    NSMutableDictionary *userSession = [@{} mutableCopy];
                    userSession[@"id"] = result.ID;
                    userSession[@"nickname"] = [result propertyForKey:@"nickname"];
                    userSession[@"access_token"] = [KOSession sharedSession].accessToken;
                    
                    // Profile Image 가 null 로 넘어오는 경우가 있어서, null check.
                    NSString *profileImage = [result propertyForKey:@"profile_image"];
                    if(profileImage && profileImage.length > 0) {
                        userSession[@"profileImage"] = profileImage;
                    }
                    
                    NSString *email = [result propertyForKey:@"email"];
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
    NSLog(@">>>>>>>>>>>>>>>>");
    NSString *urlString = [NSString stringWithFormat:@"%@", [command.arguments objectAtIndex:0]];
    NSURL *url = [NSURL URLWithString:[urlString stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    
    if ([KOSession isKakaoAccountLoginCallback:url]) {
        [KOSession handleOpenURL:url];
    }

    CDVPluginResult* pluginResult = pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
