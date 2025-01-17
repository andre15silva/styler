package de.rwth.idsg.steve.config;

import de.rwth.idsg.steve.ocpp.soap.LoggingFeatureProxy;
import de.rwth.idsg.steve.ocpp.soap.MediatorInInterceptor;
import de.rwth.idsg.steve.ocpp.soap.MessageIdInterceptor;
import de.rwth.idsg.steve.ocpp.ws.custom.AlwaysLastStrategy;
import de.rwth.idsg.steve.ocpp.ws.custom.RoundRobinStrategy;
import de.rwth.idsg.steve.ocpp.ws.custom.WsSessionSelectStrategy;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.logging.Slf4jLogger;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static de.rwth.idsg.steve.SteveConfiguration.CONFIG;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Configuration and beans related to OCPP.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 18.11.2014
 */
@Configuration
public class OcppConfiguration {

    static {
        LogUtils.setLoggerClass(Slf4jLogger.class);
    }

    @Autowired private ocpp.cs._2010._08.CentralSystemService ocpp12Server;
    @Autowired private ocpp.cs._2012._06.CentralSystemService ocpp15Server;
    @Autowired private ocpp.cs._2015._10.CentralSystemService ocpp16Server;

    @Autowired
    @Qualifier("MessageHeaderInterceptor")
    private PhaseInterceptor<Message> messageHeaderInterceptor;

    @PostConstruct
    public void init() {
        List<Interceptor<? extends Message>> interceptors = asList(new MessageIdInterceptor(), messageHeaderInterceptor);
        List<Feature> logging = singletonList(LoggingFeatureProxy.INSTANCE.get());

        createOcppService(ocpp12Server, "/CentralSystemServiceOCPP12", interceptors, logging);
        createOcppService(ocpp15Server, "/CentralSystemServiceOCPP15", interceptors, logging);
        createOcppService(ocpp16Server, "/CentralSystemServiceOCPP16", interceptors, logging);

        // Just a dummy service to route incoming messages to the appropriate service version. This should be the last
        // one to be created, since in MediatorInInterceptor we go over created/registered services and build a map.
        //
        List<Interceptor<? extends Message>> mediator = singletonList(new MediatorInInterceptor(springBus()));
        createOcppService(ocpp12Server, CONFIG.getRouterEndpointPath(), mediator, Collections.emptyList());
    }

    @Bean(name = Bus.DEFAULT_BUS_ID, destroyMethod = "shutdown")
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public WsSessionSelectStrategy sessionSelectStrategy() {
        switch (CONFIG.getOcpp().getWsSessionSelectStrategy()) {
            case ALWAYS_LAST:
                return new AlwaysLastStrategy();
            case ROUND_ROBIN:
                return new RoundRobinStrategy();
            default:
                throw new RuntimeException("Could not find a valid WsSessionSelectStrategy");
        }
    }

    private void createOcppService(Object serviceBean, String address,
                                   List<Interceptor<? extends Message>> interceptors,
                                   Collection<? extends Feature> features) {
        JaxWsServerFactoryBean f = new JaxWsServerFactoryBean();
        f.setBus(springBus());
        f.setServiceBean(serviceBean);
        f.setAddress(address);
        f.getFeatures().addAll(features);
        f.getInInterceptors().addAll(interceptors);
        f.create();
    }
}
