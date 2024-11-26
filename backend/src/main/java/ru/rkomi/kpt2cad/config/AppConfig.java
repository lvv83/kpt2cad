package ru.rkomi.kpt2cad.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ru.agiks.kptservice.config.XmlConfig.class) // импортируем конфигурацию из другого пакета
@ComponentScan(basePackages = "ru.agiks.kptservice") // Добавляем пакет для поиска компонентов
public class AppConfig {
}
