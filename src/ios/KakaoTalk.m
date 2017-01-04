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
			        NSLog(@"nickName=%@", [result propertyForKey:@"nickname"]);
                    NSLog(@"profileImage=%@", [result propertyForKey:@"profile_image"]);
                    NSLog(@"accessToken=%@", [KOSession sharedSession].accessToken);
			        
			        NSDictionary *userSession = @{
										  @"id": result.ID,
										  @"nickname": [result propertyForKey:@"nickname"],
										  @"profile_image": [result propertyForKey:@"profile_image"],
										  @"access_token": [KOSession sharedSession].accessToken};
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
