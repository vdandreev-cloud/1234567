package model;

import exception.TourServiceValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Booking {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final String bookingId;
    private final Client client;
    private final Map<TourService, Integer> serviceParticipants;
    private final LocalDate bookingDate;
    private BookingStatus status;

    // Конструктор с валидацией
    public Booking(Client client, Map<TourService, Integer> serviceParticipants, LocalDate bookingDate) {
        this.bookingId = generateBookingId(LocalDate.now());
        this.client = client;
        this.serviceParticipants = new HashMap<>(serviceParticipants);
        this.bookingDate = bookingDate;
        this.status = BookingStatus.PENDING;
    }

    // Генерация ID: BK + timestamp + 4 случайные цифры
    private String generateBookingId(LocalDate timestamp) {
        String timestampStr = timestamp.format(TIMESTAMP_FORMATTER);
        int randomSuffix = (int) (Math.random() * 10000);
        return String.format("BK%s%04d", timestampStr, randomSuffix);
    }

    public void addService(TourService service, int participants) throws TourServiceValidationException {
        validateService(service, participants);
        serviceParticipants.put(service, participants);
    }

    public void removeService(TourService service) throws TourServiceValidationException {
        if (service == null) {
            throw new TourServiceValidationException("Услуга не может быть null");
        }

        if (!serviceParticipants.containsKey(service)) {
            throw new TourServiceValidationException("Услуга не найдена");
        }

        serviceParticipants.remove(service);
    }

    public void updateParticipants(TourService service, int newParticipants) throws TourServiceValidationException {
        if (!serviceParticipants.containsKey(service)) {
            throw new TourServiceValidationException("Услуга не найдена");
        }
        validateService(service, newParticipants);
        serviceParticipants.put(service, newParticipants);
    }

    private void validateService(TourService service, int participants) throws TourServiceValidationException {
        if (service == null) {
            throw new TourServiceValidationException("Услуга не может быть null");
        }
        if (participants <= 0) {
            throw new TourServiceValidationException("Количество участников должно быть больше 0");
        }

        if (!service.isAvailableOn(LocalDate.now())) {
            throw new TourServiceValidationException("Услуга недоступна сегодня");
        }

        if (service instanceof HotelStay hotel) {
            int maxParticipants;
            switch (hotel.getRoomType()) {
                case SINGLE:
                    maxParticipants = 1;
                    break;
                case DOUBLE:
                    maxParticipants = 2;
                    break;
                case FAMILY:
                    maxParticipants = 4;
                    break;
                default:
                    maxParticipants = Integer.MAX_VALUE;
            }

            if (participants > maxParticipants) {
                throw new TourServiceValidationException(
                        "Для услуги \"" + service.getName() + "\" максимальное количество участников: " + maxParticipants
                );
            }
        }
    }

    public BigDecimal calculateTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<TourService, Integer> entry : serviceParticipants.entrySet()) {
            BigDecimal price = entry.getKey().getPrice();
            BigDecimal participants = BigDecimal.valueOf(entry.getValue());
            total = total.add(price.multiply(participants));
        }

        // Применяем скидку за баллы лояльности (1 балл = 1% скидки)
        BigDecimal discountPercent = BigDecimal.valueOf(client.getLoyaltyPoints());
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                discountPercent.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );

        return total.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
    }

    public void confirm() throws TourServiceValidationException {
        if (status != BookingStatus.PENDING) {
            throw new TourServiceValidationException("Нельзя подтвердить бронирование");
        }
        status = BookingStatus.CONFIRMED;
    }

    public void complete() throws TourServiceValidationException {
        if (status != BookingStatus.CONFIRMED) {
            throw new TourServiceValidationException("Нельзя завершить бронирование");
        }

        BigDecimal totalAmount = calculateTotalAmount();
        int pointsToAdd = totalAmount.multiply(BigDecimal.valueOf(0.1)).intValue();
        client.addLoyaltyPoints(pointsToAdd);
        status = BookingStatus.COMPLETED;
    }

    public void cancel() throws TourServiceValidationException {
        if (status != BookingStatus.PENDING && status != BookingStatus.CONFIRMED) {
            throw new TourServiceValidationException("Нельзя отменить бронирование");
        }
        status = BookingStatus.CANCELLED;
    }

    // Getters
    public String getBookingId() {
        return bookingId;
    }

    public Client getClient() {
        return client;
    }

    public Map<TourService, Integer> getServiceParticipants() {
        return new HashMap<>(serviceParticipants);
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public BookingStatus getStatus() {
        return status;
    }
}