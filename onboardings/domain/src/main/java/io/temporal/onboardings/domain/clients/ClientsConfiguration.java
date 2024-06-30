package io.temporal.onboardings.domain.clients;

import io.temporal.onboardings.domain.clients.crm.InMemoryCrmClient;
import io.temporal.onboardings.domain.clients.email.InMemoryEmailClient;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackageClasses = {InMemoryEmailClient.class, InMemoryCrmClient.class})
public class ClientsConfiguration {}
