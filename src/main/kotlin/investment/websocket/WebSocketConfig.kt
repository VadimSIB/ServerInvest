package investment.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableScheduling //@ComponentScan("org.springframework.samples")
@EnableWebSocketMessageBroker
open class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    // взаимодействие "публикация-подписка"
    // в файле js: stompClient.subscribe('/topic/loops', function (message) {
    // 				console.log(loopEvent);
    // 				var loopEvent = JSON.parse(message.body); ...
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic")
        ////	registry.enableStompBrokerRelay("/queue/", "/topic/");
//		registry.setApplicationDestinationPrefixes("/app");
        registry.setPreservePublishOrder(true)
    }

    // инициирует "рукопожатие" websocket
    // в файле js:	var socket = new SockJS('/websocket');
    //   			stompClient = Stomp.over(socket);
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/websocket") //				.setAllowedOrigins("http://localhost:3000")
                .setAllowedOrigins("*")
                .withSockJS()
    }
}
