/**
 * This code was generated by
 * \ / _    _  _|   _  _
 *  | (_)\/(_)(_|\/| |(/_  v1.0.0
 *       /       /
 */

package com.twilio.rest.ipmessaging.v2;

import com.twilio.base.Updater;
import com.twilio.converter.Promoter;
import com.twilio.exception.ApiConnectionException;
import com.twilio.exception.ApiException;
import com.twilio.exception.RestException;
import com.twilio.http.HttpMethod;
import com.twilio.http.Request;
import com.twilio.http.Response;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.Domains;

import java.net.URI;
import java.util.List;

public class ServiceUpdater extends Updater<Service> {
    private final String pathSid;
    private String friendlyName;
    private String defaultServiceRoleSid;
    private String defaultChannelRoleSid;
    private String defaultChannelCreatorRoleSid;
    private Boolean readStatusEnabled;
    private Boolean reachabilityEnabled;
    private Integer typingIndicatorTimeout;
    private Integer consumptionReportInterval;
    private Boolean notificationsNewMessageEnabled;
    private String notificationsNewMessageTemplate;
    private String notificationsNewMessageSound;
    private Boolean notificationsNewMessageBadgeCountEnabled;
    private Boolean notificationsAddedToChannelEnabled;
    private String notificationsAddedToChannelTemplate;
    private String notificationsAddedToChannelSound;
    private Boolean notificationsRemovedFromChannelEnabled;
    private String notificationsRemovedFromChannelTemplate;
    private String notificationsRemovedFromChannelSound;
    private Boolean notificationsInvitedToChannelEnabled;
    private String notificationsInvitedToChannelTemplate;
    private String notificationsInvitedToChannelSound;
    private URI preWebhookUrl;
    private URI postWebhookUrl;
    private HttpMethod webhookMethod;
    private List<String> webhookFilters;
    private Integer limitsChannelMembers;
    private Integer limitsUserChannels;
    private String mediaCompatibilityMessage;
    private Integer preWebhookRetryCount;
    private Integer postWebhookRetryCount;
    private Boolean notificationsLogEnabled;

    /**
     * Construct a new ServiceUpdater.
     *
     * @param pathSid The SID of the Service resource to update
     */
    public ServiceUpdater(final String pathSid) {
        this.pathSid = pathSid;
    }

    /**
     * A descriptive string that you create to describe the resource..
     *
     * @param friendlyName A string to describe the resource
     * @return this
     */
    public ServiceUpdater setFriendlyName(final String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    /**
     * The service role assigned to users when they are added to the service. See
     * the [Role resource](https://www.twilio.com/docs/chat/api/roles) for more info
     * about roles..
     *
     * @param defaultServiceRoleSid The service role assigned to users when they
     *                              are added to the service
     * @return this
     */
    public ServiceUpdater setDefaultServiceRoleSid(final String defaultServiceRoleSid) {
        this.defaultServiceRoleSid = defaultServiceRoleSid;
        return this;
    }

    /**
     * The channel role assigned to users when they are added to a channel. See the
     * [Role resource](https://www.twilio.com/docs/chat/api/roles) for more info
     * about roles..
     *
     * @param defaultChannelRoleSid The channel role assigned to users when they
     *                              are added to a channel
     * @return this
     */
    public ServiceUpdater setDefaultChannelRoleSid(final String defaultChannelRoleSid) {
        this.defaultChannelRoleSid = defaultChannelRoleSid;
        return this;
    }

    /**
     * The channel role assigned to a channel creator when they join a new channel.
     * See the [Role resource](https://www.twilio.com/docs/chat/api/roles) for more
     * info about roles..
     *
     * @param defaultChannelCreatorRoleSid The channel role assigned to a channel
     *                                     creator when they join a new channel
     * @return this
     */
    public ServiceUpdater setDefaultChannelCreatorRoleSid(final String defaultChannelCreatorRoleSid) {
        this.defaultChannelCreatorRoleSid = defaultChannelCreatorRoleSid;
        return this;
    }

    /**
     * Whether to enable the [Message Consumption
     * Horizon](https://www.twilio.com/docs/chat/consumption-horizon) feature. The
     * default is `true`..
     *
     * @param readStatusEnabled Whether to enable the Message Consumption Horizon
     *                          feature
     * @return this
     */
    public ServiceUpdater setReadStatusEnabled(final Boolean readStatusEnabled) {
        this.readStatusEnabled = readStatusEnabled;
        return this;
    }

    /**
     * Whether to enable the [Reachability
     * Indicator](https://www.twilio.com/docs/chat/reachability-indicator) for this
     * Service instance. The default is `false`..
     *
     * @param reachabilityEnabled Whether to enable the Reachability Indicator
     *                            feature for this Service instance
     * @return this
     */
    public ServiceUpdater setReachabilityEnabled(final Boolean reachabilityEnabled) {
        this.reachabilityEnabled = reachabilityEnabled;
        return this;
    }

    /**
     * How long in seconds after a `started typing` event until clients should
     * assume that user is no longer typing, even if no `ended typing` message was
     * received.  The default is 5 seconds..
     *
     * @param typingIndicatorTimeout How long in seconds to wait before assuming
     *                               the user is no longer typing
     * @return this
     */
    public ServiceUpdater setTypingIndicatorTimeout(final Integer typingIndicatorTimeout) {
        this.typingIndicatorTimeout = typingIndicatorTimeout;
        return this;
    }

    /**
     * DEPRECATED. The interval in seconds between consumption reports submission
     * batches from client endpoints..
     *
     * @param consumptionReportInterval DEPRECATED
     * @return this
     */
    public ServiceUpdater setConsumptionReportInterval(final Integer consumptionReportInterval) {
        this.consumptionReportInterval = consumptionReportInterval;
        return this;
    }

    /**
     * Whether to send a notification when a new message is added to a channel. The
     * default is `false`..
     *
     * @param notificationsNewMessageEnabled Whether to send a notification when a
     *                                       new message is added to a channel
     * @return this
     */
    public ServiceUpdater setNotificationsNewMessageEnabled(final Boolean notificationsNewMessageEnabled) {
        this.notificationsNewMessageEnabled = notificationsNewMessageEnabled;
        return this;
    }

    /**
     * The template to use to create the notification text displayed when a new
     * message is added to a channel and `notifications.new_message.enabled` is
     * `true`..
     *
     * @param notificationsNewMessageTemplate The template to use to create the
     *                                        notification text displayed when a new
     *                                        message is added to a channel
     * @return this
     */
    public ServiceUpdater setNotificationsNewMessageTemplate(final String notificationsNewMessageTemplate) {
        this.notificationsNewMessageTemplate = notificationsNewMessageTemplate;
        return this;
    }

    /**
     * The name of the sound to play when a new message is added to a channel and
     * `notifications.new_message.enabled` is `true`..
     *
     * @param notificationsNewMessageSound The name of the sound to play when a new
     *                                     message is added to a channel
     * @return this
     */
    public ServiceUpdater setNotificationsNewMessageSound(final String notificationsNewMessageSound) {
        this.notificationsNewMessageSound = notificationsNewMessageSound;
        return this;
    }

    /**
     * Whether the new message badge is enabled. The default is `false`..
     *
     * @param notificationsNewMessageBadgeCountEnabled Whether the new message
     *                                                 badge is enabled
     * @return this
     */
    public ServiceUpdater setNotificationsNewMessageBadgeCountEnabled(final Boolean notificationsNewMessageBadgeCountEnabled) {
        this.notificationsNewMessageBadgeCountEnabled = notificationsNewMessageBadgeCountEnabled;
        return this;
    }

    /**
     * Whether to send a notification when a member is added to a channel. The
     * default is `false`..
     *
     * @param notificationsAddedToChannelEnabled Whether to send a notification
     *                                           when a member is added to a channel
     * @return this
     */
    public ServiceUpdater setNotificationsAddedToChannelEnabled(final Boolean notificationsAddedToChannelEnabled) {
        this.notificationsAddedToChannelEnabled = notificationsAddedToChannelEnabled;
        return this;
    }

    /**
     * The template to use to create the notification text displayed when a member
     * is added to a channel and `notifications.added_to_channel.enabled` is
     * `true`..
     *
     * @param notificationsAddedToChannelTemplate The template to use to create the
     *                                            notification text displayed when a
     *                                            member is added to a channel
     * @return this
     */
    public ServiceUpdater setNotificationsAddedToChannelTemplate(final String notificationsAddedToChannelTemplate) {
        this.notificationsAddedToChannelTemplate = notificationsAddedToChannelTemplate;
        return this;
    }

    /**
     * The name of the sound to play when a member is added to a channel and
     * `notifications.added_to_channel.enabled` is `true`..
     *
     * @param notificationsAddedToChannelSound The name of the sound to play when a
     *                                         member is added to a channel
     * @return this
     */
    public ServiceUpdater setNotificationsAddedToChannelSound(final String notificationsAddedToChannelSound) {
        this.notificationsAddedToChannelSound = notificationsAddedToChannelSound;
        return this;
    }

    /**
     * Whether to send a notification to a user when they are removed from a
     * channel. The default is `false`..
     *
     * @param notificationsRemovedFromChannelEnabled Whether to send a notification
     *                                               to a user when they are removed
     *                                               from a channel
     * @return this
     */
    public ServiceUpdater setNotificationsRemovedFromChannelEnabled(final Boolean notificationsRemovedFromChannelEnabled) {
        this.notificationsRemovedFromChannelEnabled = notificationsRemovedFromChannelEnabled;
        return this;
    }

    /**
     * The template to use to create the notification text displayed to a user when
     * they are removed from a channel and
     * `notifications.removed_from_channel.enabled` is `true`..
     *
     * @param notificationsRemovedFromChannelTemplate The template to use to create
     *                                                the notification text
     *                                                displayed to a user when they
     *                                                are removed
     * @return this
     */
    public ServiceUpdater setNotificationsRemovedFromChannelTemplate(final String notificationsRemovedFromChannelTemplate) {
        this.notificationsRemovedFromChannelTemplate = notificationsRemovedFromChannelTemplate;
        return this;
    }

    /**
     * The name of the sound to play to a user when they are removed from a channel
     * and `notifications.removed_from_channel.enabled` is `true`..
     *
     * @param notificationsRemovedFromChannelSound The name of the sound to play to
     *                                             a user when they are removed from
     *                                             a channel
     * @return this
     */
    public ServiceUpdater setNotificationsRemovedFromChannelSound(final String notificationsRemovedFromChannelSound) {
        this.notificationsRemovedFromChannelSound = notificationsRemovedFromChannelSound;
        return this;
    }

    /**
     * Whether to send a notification when a user is invited to a channel. The
     * default is `false`..
     *
     * @param notificationsInvitedToChannelEnabled Whether to send a notification
     *                                             when a user is invited to a
     *                                             channel
     * @return this
     */
    public ServiceUpdater setNotificationsInvitedToChannelEnabled(final Boolean notificationsInvitedToChannelEnabled) {
        this.notificationsInvitedToChannelEnabled = notificationsInvitedToChannelEnabled;
        return this;
    }

    /**
     * The template to use to create the notification text displayed when a user is
     * invited to a channel and `notifications.invited_to_channel.enabled` is
     * `true`..
     *
     * @param notificationsInvitedToChannelTemplate The template to use to create
     *                                              the notification text displayed
     *                                              when a user is invited to a
     *                                              channel
     * @return this
     */
    public ServiceUpdater setNotificationsInvitedToChannelTemplate(final String notificationsInvitedToChannelTemplate) {
        this.notificationsInvitedToChannelTemplate = notificationsInvitedToChannelTemplate;
        return this;
    }

    /**
     * The name of the sound to play when a user is invited to a channel and
     * `notifications.invited_to_channel.enabled` is `true`..
     *
     * @param notificationsInvitedToChannelSound The name of the sound to play when
     *                                           a user is invited to a channel
     * @return this
     */
    public ServiceUpdater setNotificationsInvitedToChannelSound(final String notificationsInvitedToChannelSound) {
        this.notificationsInvitedToChannelSound = notificationsInvitedToChannelSound;
        return this;
    }

    /**
     * The URL for pre-event webhooks, which are called by using the
     * `webhook_method`. See [Webhook
     * Events](https://www.twilio.com/docs/chat/webhook-events) for more details..
     *
     * @param preWebhookUrl The webhook URL for pre-event webhooks
     * @return this
     */
    public ServiceUpdater setPreWebhookUrl(final URI preWebhookUrl) {
        this.preWebhookUrl = preWebhookUrl;
        return this;
    }

    /**
     * The URL for pre-event webhooks, which are called by using the
     * `webhook_method`. See [Webhook
     * Events](https://www.twilio.com/docs/chat/webhook-events) for more details..
     *
     * @param preWebhookUrl The webhook URL for pre-event webhooks
     * @return this
     */
    public ServiceUpdater setPreWebhookUrl(final String preWebhookUrl) {
        return setPreWebhookUrl(Promoter.uriFromString(preWebhookUrl));
    }

    /**
     * The URL for post-event webhooks, which are called by using the
     * `webhook_method`. See [Webhook
     * Events](https://www.twilio.com/docs/chat/webhook-events) for more details..
     *
     * @param postWebhookUrl The URL for post-event webhooks
     * @return this
     */
    public ServiceUpdater setPostWebhookUrl(final URI postWebhookUrl) {
        this.postWebhookUrl = postWebhookUrl;
        return this;
    }

    /**
     * The URL for post-event webhooks, which are called by using the
     * `webhook_method`. See [Webhook
     * Events](https://www.twilio.com/docs/chat/webhook-events) for more details..
     *
     * @param postWebhookUrl The URL for post-event webhooks
     * @return this
     */
    public ServiceUpdater setPostWebhookUrl(final String postWebhookUrl) {
        return setPostWebhookUrl(Promoter.uriFromString(postWebhookUrl));
    }

    /**
     * The HTTP method to use for calls to the `pre_webhook_url` and
     * `post_webhook_url` webhooks.  Can be: `POST` or `GET` and the default is
     * `POST`. See [Webhook Events](https://www.twilio.com/docs/chat/webhook-events)
     * for more details..
     *
     * @param webhookMethod The HTTP method  to use for both PRE and POST webhooks
     * @return this
     */
    public ServiceUpdater setWebhookMethod(final HttpMethod webhookMethod) {
        this.webhookMethod = webhookMethod;
        return this;
    }

    /**
     * The list of webhook events that are enabled for this Service instance. See
     * [Webhook Events](https://www.twilio.com/docs/chat/webhook-events) for more
     * details..
     *
     * @param webhookFilters The list of webhook events that are enabled for this
     *                       Service instance
     * @return this
     */
    public ServiceUpdater setWebhookFilters(final List<String> webhookFilters) {
        this.webhookFilters = webhookFilters;
        return this;
    }

    /**
     * The list of webhook events that are enabled for this Service instance. See
     * [Webhook Events](https://www.twilio.com/docs/chat/webhook-events) for more
     * details..
     *
     * @param webhookFilters The list of webhook events that are enabled for this
     *                       Service instance
     * @return this
     */
    public ServiceUpdater setWebhookFilters(final String webhookFilters) {
        return setWebhookFilters(Promoter.listOfOne(webhookFilters));
    }

    /**
     * The maximum number of Members that can be added to Channels within this
     * Service. Can be up to 1,000..
     *
     * @param limitsChannelMembers The maximum number of Members that can be added
     *                             to Channels within this Service
     * @return this
     */
    public ServiceUpdater setLimitsChannelMembers(final Integer limitsChannelMembers) {
        this.limitsChannelMembers = limitsChannelMembers;
        return this;
    }

    /**
     * The maximum number of Channels Users can be a Member of within this Service.
     * Can be up to 1,000..
     *
     * @param limitsUserChannels The maximum number of Channels Users can be a
     *                           Member of within this Service
     * @return this
     */
    public ServiceUpdater setLimitsUserChannels(final Integer limitsUserChannels) {
        this.limitsUserChannels = limitsUserChannels;
        return this;
    }

    /**
     * The message to send when a media message has no text. Can be used as
     * placeholder message..
     *
     * @param mediaCompatibilityMessage The message to send when a media message
     *                                  has no text
     * @return this
     */
    public ServiceUpdater setMediaCompatibilityMessage(final String mediaCompatibilityMessage) {
        this.mediaCompatibilityMessage = mediaCompatibilityMessage;
        return this;
    }

    /**
     * The number of times to retry a call to the `pre_webhook_url` if the request
     * times out (after 5 seconds) or it receives a 429, 503, or 504 HTTP response.
     * Default retry count is 0 times, which means the call won't be retried..
     *
     * @param preWebhookRetryCount Count of times webhook will be retried in case
     *                             of timeout or 429/503/504 HTTP responses
     * @return this
     */
    public ServiceUpdater setPreWebhookRetryCount(final Integer preWebhookRetryCount) {
        this.preWebhookRetryCount = preWebhookRetryCount;
        return this;
    }

    /**
     * The number of times to retry a call to the `post_webhook_url` if the request
     * times out (after 5 seconds) or it receives a 429, 503, or 504 HTTP response.
     * The default is 0, which means the call won't be retried..
     *
     * @param postWebhookRetryCount The number of times calls to the
     *                              `post_webhook_url` will be retried
     * @return this
     */
    public ServiceUpdater setPostWebhookRetryCount(final Integer postWebhookRetryCount) {
        this.postWebhookRetryCount = postWebhookRetryCount;
        return this;
    }

    /**
     * Whether to log notifications. The default is `false`..
     *
     * @param notificationsLogEnabled Whether to log notifications
     * @return this
     */
    public ServiceUpdater setNotificationsLogEnabled(final Boolean notificationsLogEnabled) {
        this.notificationsLogEnabled = notificationsLogEnabled;
        return this;
    }

    /**
     * Make the request to the Twilio API to perform the update.
     *
     * @param client TwilioRestClient with which to make the request
     * @return Updated Service
     */
    @Override
    @SuppressWarnings("checkstyle:linelength")
    public Service update(final TwilioRestClient client) {
        Request request = new Request(
            HttpMethod.POST,
            Domains.IPMESSAGING.toString(),
            "/v2/Services/" + this.pathSid + "",
            client.getRegion()
        );

        addPostParams(request);
        Response response = client.request(request);

        if (response == null) {
            throw new ApiConnectionException("Service update failed: Unable to connect to server");
        } else if (!TwilioRestClient.SUCCESS.apply(response.getStatusCode())) {
            RestException restException = RestException.fromJson(response.getStream(), client.getObjectMapper());
            if (restException == null) {
                throw new ApiException("Server Error, no content");
            }

            throw new ApiException(
                restException.getMessage(),
                restException.getCode(),
                restException.getMoreInfo(),
                restException.getStatus(),
                null
            );
        }

        return Service.fromJson(response.getStream(), client.getObjectMapper());
    }

    /**
     * Add the requested post parameters to the Request.
     *
     * @param request Request to add post params to
     */
    private void addPostParams(final Request request) {
        if (friendlyName != null) {
            request.addPostParam("FriendlyName", friendlyName);
        }

        if (defaultServiceRoleSid != null) {
            request.addPostParam("DefaultServiceRoleSid", defaultServiceRoleSid);
        }

        if (defaultChannelRoleSid != null) {
            request.addPostParam("DefaultChannelRoleSid", defaultChannelRoleSid);
        }

        if (defaultChannelCreatorRoleSid != null) {
            request.addPostParam("DefaultChannelCreatorRoleSid", defaultChannelCreatorRoleSid);
        }

        if (readStatusEnabled != null) {
            request.addPostParam("ReadStatusEnabled", readStatusEnabled.toString());
        }

        if (reachabilityEnabled != null) {
            request.addPostParam("ReachabilityEnabled", reachabilityEnabled.toString());
        }

        if (typingIndicatorTimeout != null) {
            request.addPostParam("TypingIndicatorTimeout", typingIndicatorTimeout.toString());
        }

        if (consumptionReportInterval != null) {
            request.addPostParam("ConsumptionReportInterval", consumptionReportInterval.toString());
        }

        if (notificationsNewMessageEnabled != null) {
            request.addPostParam("Notifications.NewMessage.Enabled", notificationsNewMessageEnabled.toString());
        }

        if (notificationsNewMessageTemplate != null) {
            request.addPostParam("Notifications.NewMessage.Template", notificationsNewMessageTemplate);
        }

        if (notificationsNewMessageSound != null) {
            request.addPostParam("Notifications.NewMessage.Sound", notificationsNewMessageSound);
        }

        if (notificationsNewMessageBadgeCountEnabled != null) {
            request.addPostParam("Notifications.NewMessage.BadgeCountEnabled", notificationsNewMessageBadgeCountEnabled.toString());
        }

        if (notificationsAddedToChannelEnabled != null) {
            request.addPostParam("Notifications.AddedToChannel.Enabled", notificationsAddedToChannelEnabled.toString());
        }

        if (notificationsAddedToChannelTemplate != null) {
            request.addPostParam("Notifications.AddedToChannel.Template", notificationsAddedToChannelTemplate);
        }

        if (notificationsAddedToChannelSound != null) {
            request.addPostParam("Notifications.AddedToChannel.Sound", notificationsAddedToChannelSound);
        }

        if (notificationsRemovedFromChannelEnabled != null) {
            request.addPostParam("Notifications.RemovedFromChannel.Enabled", notificationsRemovedFromChannelEnabled.toString());
        }

        if (notificationsRemovedFromChannelTemplate != null) {
            request.addPostParam("Notifications.RemovedFromChannel.Template", notificationsRemovedFromChannelTemplate);
        }

        if (notificationsRemovedFromChannelSound != null) {
            request.addPostParam("Notifications.RemovedFromChannel.Sound", notificationsRemovedFromChannelSound);
        }

        if (notificationsInvitedToChannelEnabled != null) {
            request.addPostParam("Notifications.InvitedToChannel.Enabled", notificationsInvitedToChannelEnabled.toString());
        }

        if (notificationsInvitedToChannelTemplate != null) {
            request.addPostParam("Notifications.InvitedToChannel.Template", notificationsInvitedToChannelTemplate);
        }

        if (notificationsInvitedToChannelSound != null) {
            request.addPostParam("Notifications.InvitedToChannel.Sound", notificationsInvitedToChannelSound);
        }

        if (preWebhookUrl != null) {
            request.addPostParam("PreWebhookUrl", preWebhookUrl.toString());
        }

        if (postWebhookUrl != null) {
            request.addPostParam("PostWebhookUrl", postWebhookUrl.toString());
        }

        if (webhookMethod != null) {
            request.addPostParam("WebhookMethod", webhookMethod.toString());
        }

        if (webhookFilters != null) {
            for (String prop : webhookFilters) {
                request.addPostParam("WebhookFilters", prop);
            }
        }

        if (limitsChannelMembers != null) {
            request.addPostParam("Limits.ChannelMembers", limitsChannelMembers.toString());
        }

        if (limitsUserChannels != null) {
            request.addPostParam("Limits.UserChannels", limitsUserChannels.toString());
        }

        if (mediaCompatibilityMessage != null) {
            request.addPostParam("Media.CompatibilityMessage", mediaCompatibilityMessage);
        }

        if (preWebhookRetryCount != null) {
            request.addPostParam("PreWebhookRetryCount", preWebhookRetryCount.toString());
        }

        if (postWebhookRetryCount != null) {
            request.addPostParam("PostWebhookRetryCount", postWebhookRetryCount.toString());
        }

        if (notificationsLogEnabled != null) {
            request.addPostParam("Notifications.LogEnabled", notificationsLogEnabled.toString());
        }
    }
}