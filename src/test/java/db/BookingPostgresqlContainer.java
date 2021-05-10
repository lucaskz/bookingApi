package db;

import org.testcontainers.containers.PostgreSQLContainer;

public class BookingPostgresqlContainer extends PostgreSQLContainer<BookingPostgresqlContainer> {
  private static final String IMAGE_VERSION = "postgres:13.3";
  private static BookingPostgresqlContainer container;

  private BookingPostgresqlContainer() {
    super(IMAGE_VERSION);
  }

  public static BookingPostgresqlContainer getInstance() {
    if (container == null) {
      container = new BookingPostgresqlContainer();
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    System.setProperty("DB_URL", container.getJdbcUrl());
    System.setProperty("DB_USERNAME", container.getUsername());
    System.setProperty("DB_PASSWORD", container.getPassword());
    this.setPrivilegedMode(true);
  }

  @Override
  public void stop() {
    // do nothing, JVM handles shut down
  }
}
