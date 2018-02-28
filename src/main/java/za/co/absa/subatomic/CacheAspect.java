package za.co.absa.subatomic;

import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CacheAspect {

    private static final Logger log = LoggerFactory
            .getLogger(CacheAspect.class);

    private CacheManager cacheManager;

    public CacheAspect(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Around("execution(* org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepository.findOne(..))")
    public Object findOnePointCut(final ProceedingJoinPoint joinPoint)
            throws Throwable {
        String app = (String) joinPoint.getArgs()[0];
        String profile = (String) joinPoint.getArgs()[1];
        String label = ArrayUtils.toString(joinPoint.getArgs()[2], null);
        String cacheIdentifier = String.format("%s-%s-%s", app, profile, label);
        log.debug("Checking cache for: [{}]", cacheIdentifier);

        Cache cache = cacheManager.getCache("configServer");
        Object environment;
        if (cache.get(cacheIdentifier) != null) {
            log.debug("Using cached environment: {}", cache);
            environment = cache.get(cacheIdentifier).get();
        }
        else {
            environment = joinPoint.proceed();
            cache.put(cacheIdentifier, environment);
            log.debug("Cached environment: {}", environment);
        }

        return environment;
    }

    @Around("execution(* org.springframework.cloud.endpoint.RefreshEndpoint..*(..))")
    public Object refreshPointCut(final ProceedingJoinPoint joinPoint)
            throws Throwable {
        log.info("Clearing cache...");
        cacheManager.getCache("configServer").clear();
        return joinPoint.proceed();
    }
}
