package com.db.dataplatform.techtest.server.component.impl;

import com.db.dataplatform.techtest.server.component.RandomWorkDurationGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
public class RandomWorkDurationGeneratorImpl implements RandomWorkDurationGenerator {
    @Override
    public Integer generate() {
        final int random = new Random().ints(2000, 4000).findAny().getAsInt();
        log.info("generated random int: {}", random);
        return random;
    }
}
