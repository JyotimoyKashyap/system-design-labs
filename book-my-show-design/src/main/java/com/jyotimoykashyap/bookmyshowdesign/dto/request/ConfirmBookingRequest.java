package com.jyotimoykashyap.bookmyshowdesign.dto.request;

import com.jyotimoykashyap.bookmyshowdesign.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmBookingRequest {
    private int userId;
    private PaymentMethod paymentMethod;
}

