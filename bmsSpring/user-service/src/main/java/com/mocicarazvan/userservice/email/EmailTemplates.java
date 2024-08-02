package com.mocicarazvan.userservice.email;

public class EmailTemplates {
    public static String verifyEmail(String frontUrl, String confirmUrl) {
        return STR."""
                                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                <html dir="ltr" lang="en">

                  <head>
                    <link rel="preload" as="image" href="https://res.cloudinary.com/lamatutorial/image/upload/v1722268675/logo_i2scwt.svg" />
                    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />
                    <meta name="x-apple-disable-message-reformatting" />
                  </head>

                  <body style="font-family:-apple-system,BlinkMacSystemFont,&quot;Segoe UI&quot;,Roboto,&quot;Helvetica Neue&quot;,Ubuntu,sans-serif;background-color:rgb(246,249,252)">
                    <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;padding-top:1.25rem;padding-bottom:1.25rem">
                      <tbody>
                        <tr style="width:100%">
                          <td>
                            <table align="center" width="100%" class="bg-backround" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;box-shadow:0 0 #0000, 0 0 #0000, 0 20px 25px -5px rgb(0,0,0,0.1), 0 8px 10px -6px rgb(0,0,0,0.1);border-radius:0.25rem">
                              <tbody>
                                <tr style="width:100%">
                                  <td>
                                    <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="height:100px;background-color:rgb(148,163,184);border-top-left-radius:0.25rem;border-top-right-radius:0.25rem;justify-content:center;padding-left:1.25rem;padding-right:1.25rem;width:100%">
                                      <tbody>
                                        <tr>
                                          <td>
                                            <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="width:100%">
                                              <tbody style="width:100%">
                                                <tr style="width:100%">
                                                  <td data-id="__react-email-column" style="text-align:start"><a href=\{frontUrl} target="_blank" style="font-size:1.5rem;line-height:2rem;font-weight:700;letter-spacing:-0.05em;margin-right:1rem;text-decoration-line:none;color:inherit">Bro Meets Science</a> </td>
                                                  <td data-id="__react-email-column" style="text-align:end"><img src="https://res.cloudinary.com/lamatutorial/image/upload/v1722269171/logo_i2scwt_2_we0jmo.png" alt="Logo" width="80" height="80" /></td>
                                                </tr>
                                              </tbody>
                                            </table>
                                          </td>
                                        </tr>
                                      </tbody>
                                    </table>
                                    <table align="center" width="100%" class="space-y-10" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="padding:1rem;padding-top:2.5rem;padding-bottom:2.5rem">
                                      <tbody>
                                        <tr>
                                          <td>
                                            <h1 style="text-align:center">Please confirm your email</h1>
                                            <p style="font-size:1.125rem;line-height:1.75rem;margin:16px 0">An email confirmation has been sent to your email address. Please click the link in the email to confirm your email address.</p>
                                            <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;text-align:center;margin-top:2.5rem">
                                              <tbody>
                                                <tr style="width:100%">
                                                  <td><a class="  hover:bg-slate-600 hover:text-slate-100 hover:scale-105    " href=\{confirmUrl} style="line-height:1.75rem;text-decoration:none;display:inline-block;max-width:100%;background-color:rgb(148,163,184);padding-top:0.5rem;padding-bottom:0.5rem;padding-left:1rem;padding-right:1rem;border-radius:0.25rem;color:rgb(248,250,252);font-size:1.125rem;font-weight:600;margin-left:auto;margin-right:auto;padding:8px 16px 8px 16px" target="_blank"><span><!--[if mso]><i style="mso-font-width:400%;mso-text-raise:12" hidden>&#8202;&#8202;</i><![endif]--></span><span style="max-width:100%;display:inline-block;line-height:120%;mso-padding-alt:0px;mso-text-raise:6px">Click To Confirm</span><span><!--[if mso]><i style="mso-font-width:400%" hidden>&#8202;&#8202;&#8203;</i><![endif]--></span></a></td>
                                                </tr>
                                              </tbody>
                                            </table>
                                          </td>
                                        </tr>
                                      </tbody>
                                    </table>
                                    <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="height:50px;background-color:rgb(148,163,184);border-bottom-right-radius:0.25rem;border-bottom-left-radius:0.25rem">
                                      <tbody>
                                        <tr>
                                          <td>
                                            <p style="font-size:17px;line-height:24px;margin:16px 0;text-align:center">For any information, please contact us at<!-- --> <a href="mailto:razvanmocica@gmail.com" style="font-weight:700;letter-spacing:-0.05em;text-decoration-line:none;color:inherit">razvanmocica@gmail.com</a></p>
                                          </td>
                                        </tr>
                                      </tbody>
                                    </table>
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </body>

                </html>

                                                                """;
    }

    public static String resetPassword(String frontUrl, String resetUrl) {
        return STR."""
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                <html dir="ltr" lang="en">
                                
                  <head>
                    <link rel="preload" as="image" href="https://res.cloudinary.com/lamatutorial/image/upload/v1722269171/logo_i2scwt_2_we0jmo.png" />
                    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />
                    <meta name="x-apple-disable-message-reformatting" />
                  </head>
                                
                  <body style="font-family:-apple-system,BlinkMacSystemFont,&quot;Segoe UI&quot;,Roboto,&quot;Helvetica Neue&quot;,Ubuntu,sans-serif;background-color:rgb(246,249,252)">
                    <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;padding-top:1.25rem;padding-bottom:1.25rem">
                      <tbody>
                        <tr style="width:100%">
                          <td>
                            <table align="center" width="100%" class="bg-backround" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;box-shadow:0 0 #0000, 0 0 #0000, 0 20px 25px -5px rgb(0,0,0,0.1), 0 8px 10px -6px rgb(0,0,0,0.1);border-radius:0.25rem">
                              <tbody>
                                <tr style="width:100%">
                                  <td>
                                    <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="height:100px;background-color:rgb(148,163,184);border-top-left-radius:0.25rem;border-top-right-radius:0.25rem;justify-content:center;padding-left:1.25rem;padding-right:1.25rem;width:100%">
                                      <tbody>
                                        <tr>
                                          <td>
                                            <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="width:100%">
                                              <tbody style="width:100%">
                                                <tr style="width:100%">
                                                  <td data-id="__react-email-column" style="text-align:start"><a href=\{frontUrl} target="_blank" style="font-size:1.5rem;line-height:2rem;font-weight:700;letter-spacing:-0.05em;margin-right:1rem;text-decoration-line:none;color:inherit">Bro Meets Science</a> </td>
                                                  <td data-id="__react-email-column" style="text-align:end"><img src="https://res.cloudinary.com/lamatutorial/image/upload/v1722269171/logo_i2scwt_2_we0jmo.png" alt="Logo" width="80" height="80" /></td>
                                                </tr>
                                              </tbody>
                                            </table>
                                          </td>
                                        </tr>
                                      </tbody>
                                    </table>
                                    <table align="center" width="100%" class="space-y-10" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="padding:1rem;padding-top:2.5rem;padding-bottom:2.5rem">
                                      <tbody>
                                        <tr>
                                          <td>
                                            <h1 style="text-align:center">Reset Your Password</h1>
                                            <p style="font-size:1.125rem;line-height:1.75rem;margin:16px 0">We received a request to reset your password. Click the button below to confirm this action.</p>
                                            <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;text-align:center;margin-top:2.5rem">
                                              <tbody>
                                                <tr style="width:100%">
                                                  <td><a class="  hover:bg-slate-600 hover:text-slate-100 hover:scale-105    " href=\{resetUrl} style="line-height:1.75rem;text-decoration:none;display:inline-block;max-width:100%;background-color:rgb(148,163,184);padding-top:0.5rem;padding-bottom:0.5rem;padding-left:1rem;padding-right:1rem;border-radius:0.25rem;color:rgb(248,250,252);font-size:1.125rem;font-weight:600;margin-left:auto;margin-right:auto;padding:8px 16px 8px 16px" target="_blank"><span><!--[if mso]><i style="mso-font-width:400%;mso-text-raise:12" hidden>&#8202;&#8202;</i><![endif]--></span><span style="max-width:100%;display:inline-block;line-height:120%;mso-padding-alt:0px;mso-text-raise:6px">Click To Change Password</span><span><!--[if mso]><i style="mso-font-width:400%" hidden>&#8202;&#8202;&#8203;</i><![endif]--></span></a></td>
                                                </tr>
                                              </tbody>
                                            </table>
                                          </td>
                                        </tr>
                                      </tbody>
                                    </table>
                                    <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="height:50px;background-color:rgb(148,163,184);border-bottom-right-radius:0.25rem;border-bottom-left-radius:0.25rem">
                                      <tbody>
                                        <tr>
                                          <td>
                                            <p style="font-size:17px;line-height:24px;margin:16px 0;text-align:center">For any information, please contact us at<!-- --> <a href="mailto:razvanmocica@gmail.com" style="font-weight:700;letter-spacing:-0.05em;text-decoration-line:none;color:inherit">razvanmocica@gmail.com</a></p>
                                          </td>
                                        </tr>
                                      </tbody>
                                    </table>
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </body>
                                
                </html>
                """;
    }

    public static String adminTemplate(String frontUrl, String content) {
        return STR."""
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                <html dir="ltr" lang="en">
                                
                  <head>
                    <link rel="preload" as="image" href="https://res.cloudinary.com/lamatutorial/image/upload/v1722269171/logo_i2scwt_2_we0jmo.png" />
                    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />
                    <meta name="x-apple-disable-message-reformatting" />
                  </head>
                                
                  <body style="font-family:-apple-system,BlinkMacSystemFont,&quot;Segoe UI&quot;,Roboto,&quot;Helvetica Neue&quot;,Ubuntu,sans-serif;background-color:rgb(246,249,252)">
                    <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;padding-top:1.25rem;padding-bottom:1.25rem">
                      <tbody>
                        <tr style="width:100%">
                          <td>
                            <table align="center" width="100%" class="bg-backround" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;box-shadow:0 0 #0000, 0 0 #0000, 0 20px 25px -5px rgb(0,0,0,0.1), 0 8px 10px -6px rgb(0,0,0,0.1);border-radius:0.25rem">
                              <tbody>
                                <tr style="width:100%">
                                  <td>
                                    <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="height:100px;background-color:rgb(148,163,184);border-top-left-radius:0.25rem;border-top-right-radius:0.25rem;justify-content:center;padding-left:1.25rem;padding-right:1.25rem;width:100%">
                                      <tbody>
                                        <tr>
                                          <td>
                                            <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="width:100%">
                                              <tbody style="width:100%">
                                                <tr style="width:100%">
                                                  <td data-id="__react-email-column" style="text-align:start"><a href=\{frontUrl} target="_blank" style="font-size:1.5rem;line-height:2rem;font-weight:700;letter-spacing:-0.05em;margin-right:1rem;text-decoration-line:none;color:inherit">Bro Meets Science</a> </td>
                                                  <td data-id="__react-email-column" style="text-align:end"><img src="https://res.cloudinary.com/lamatutorial/image/upload/v1722269171/logo_i2scwt_2_we0jmo.png" alt="Logo" width="80" height="80" /></td>
                                                </tr>
                                              </tbody>
                                            </table>
                                          </td>
                                        </tr>
                                      </tbody>
                                    </table>
                                    <table align="center" width="100%" class="space-y-2 " border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;margin-top:2.5rem;margin-bottom:2.5rem;padding-left:0.5rem;padding-right:0.5rem">
                                      <tbody>
                                        <tr style="width:100%">
                                          <td>\{content}</td>
                                        </tr>
                                      </tbody>
                                    </table>
                                    <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="height:50px;background-color:rgb(148,163,184);border-bottom-right-radius:0.25rem;border-bottom-left-radius:0.25rem">
                                      <tbody>
                                        <tr>
                                          <td>
                                            <p style="font-size:17px;line-height:24px;margin:16px 0;text-align:center">For any information, please contact us at<!-- --> <a href="mailto:razvanmocica@gmail.com" style="font-weight:700;letter-spacing:-0.05em;text-decoration-line:none;color:inherit">razvanmocica@gmail.com</a></p>
                                          </td>
                                        </tr>
                                      </tbody>
                                    </table>
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </body>
                                
                </html>
                """;
    }
}
