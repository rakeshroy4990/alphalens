package com.alphalens.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcUrlEnvironmentPostProcessorTest {

    @Test
    void rewritesPostgresSchemeSubstitutesPasswordAndEnablesSsl() {
        var resolved = JdbcUrlEnvironmentPostProcessor.resolve(
                "postgresql://postgres.nbityfnkiavbzkkacaxm:{{password}}@aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres",
                "s3cret"
        );

        assertThat(resolved.url()).isEqualTo(
                "jdbc:postgresql://aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
        );
        assertThat(resolved.username()).isEqualTo("postgres.nbityfnkiavbzkkacaxm");
        assertThat(resolved.password()).isEqualTo("s3cret");
        assertThat(resolved.transactionPooler()).isFalse();
    }

    @Test
    void acceptsSupabaseDirectUri() {
        var resolved = JdbcUrlEnvironmentPostProcessor.resolve(
                "postgresql://postgres:{{password}}@db.nbityfnkiavbzkkacaxm.supabase.co:5432/postgres",
                "s3cret"
        );

        assertThat(resolved.url()).isEqualTo(
                "jdbc:postgresql://db.nbityfnkiavbzkkacaxm.supabase.co:5432/postgres?sslmode=require"
        );
        assertThat(resolved.username()).isEqualTo("postgres");
        assertThat(resolved.password()).isEqualTo("s3cret");
    }

    @Test
    void keepsSecretPasswordOutOfUriWhenPlaceholderIsUsed() {
        var resolved = JdbcUrlEnvironmentPostProcessor.resolve(
                "postgresql://postgres:{{password}}@db.nbityfnkiavbzkkacaxm.supabase.co:5432/postgres",
                "p@ss:word/with#special"
        );

        assertThat(resolved.url()).isEqualTo(
                "jdbc:postgresql://db.nbityfnkiavbzkkacaxm.supabase.co:5432/postgres?sslmode=require"
        );
        assertThat(resolved.username()).isEqualTo("postgres");
        assertThat(resolved.password()).isEqualTo("p@ss:word/with#special");
    }

    @Test
    void rewritesPostgresqlSchemeToJdbc() {
        assertThat(JdbcUrlEnvironmentPostProcessor.normalize(
                "postgresql://localhost:5432/alphalens"
        )).isEqualTo("jdbc:postgresql://localhost:5432/alphalens");
    }

    @Test
    void leavesJdbcUrlsUnchanged() {
        String url = "jdbc:postgresql://localhost:5432/alphalens?sslmode=disable";
        assertThat(JdbcUrlEnvironmentPostProcessor.normalize(url)).isEqualTo(url);
    }

    @Test
    void rejectsPlaceholderWithoutPassword() {
        assertThatThrownBy(() -> JdbcUrlEnvironmentPostProcessor.resolve(
                "postgresql://postgres.ref:{{password}}@aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres",
                ""
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SPRING_DATASOURCE_PASSWORD");
    }

    @Test
    void postProcessWritesJdbcUrlIntoSystemProperties() {
        String previousUrl = System.getProperty("SPRING_DATASOURCE_URL");
        String previousSpring = System.getProperty("spring.datasource.url");
        String previousUser = System.getProperty("spring.datasource.username");
        String previousPassword = System.getProperty("spring.datasource.password");
        String previousEnvUser = System.getProperty("SPRING_DATASOURCE_USERNAME");
        String previousEnvPassword = System.getProperty("SPRING_DATASOURCE_PASSWORD");
        try {
            MockEnvironment environment = new MockEnvironment();
            environment.setProperty(
                    "SPRING_DATASOURCE_URL",
                    "postgresql://alphalens:alphalens@localhost:5432/alphalens"
            );

            new JdbcUrlEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

            assertThat(environment.getProperty("spring.datasource.url"))
                    .startsWith("jdbc:postgresql://");
            assertThat(System.getProperty("SPRING_DATASOURCE_URL"))
                    .isEqualTo("jdbc:postgresql://localhost:5432/alphalens");
        } finally {
            restoreSystemProperty("SPRING_DATASOURCE_URL", previousUrl);
            restoreSystemProperty("spring.datasource.url", previousSpring);
            restoreSystemProperty("spring.datasource.username", previousUser);
            restoreSystemProperty("spring.datasource.password", previousPassword);
            restoreSystemProperty("SPRING_DATASOURCE_USERNAME", previousEnvUser);
            restoreSystemProperty("SPRING_DATASOURCE_PASSWORD", previousEnvPassword);
        }
    }

    private static void restoreSystemProperty(String key, String previous) {
        if (previous == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previous);
        }
    }
}
