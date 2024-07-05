package com.example.mypos.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mypos.R;
import com.example.mypos.databinding.FragmentHomeBinding;
import com.mypos.glasssdk.MyPOSAPI;
import com.mypos.glasssdk.MyPOSVoid;
import com.mypos.glasssdk.TransactionProcessingResult;
import com.mypos.glasssdk.exceptions.FunctionalityNotSupportedException;
import com.mypos.slavesdk.ConnectionType;
import com.mypos.slavesdk.Currency;
import com.mypos.slavesdk.POSHandler;
import com.stripe.android.model.PaymentIntent;

import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback;
import com.stripe.stripeterminal.external.callable.TerminalListener;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.TerminalException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Button submitButton, submitButton2;
    private EditText amountEditText;
    private static final int VOID_REQUEST_CODE = 4;
    private static final int REQUEST_CODE_MAKE_PAYMENT = 4;

    private Button connectReaderButton;
    private Button createPaymentIntentButton;


    private POSHandler mPOSHandler;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);




        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        POSHandler.setCurrency(Currency.EUR);
        POSHandler.setApplicationContext(getContext());
        POSHandler.setDefaultReceiptConfig(4);
        POSHandler.setConnectionType(ConnectionType.BLUETOOTH); // Use ConnectionType.USB for usb connection type
        mPOSHandler = POSHandler.getInstance();


        amountEditText = requireView().findViewById(R.id.amountEditText);
        submitButton =  requireView().findViewById(R.id.submitButton);
        submitButton2 =  requireView().findViewById(R.id.submitButton2);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountText = amountEditText.getText().toString();
                if (!amountText.isEmpty()) {
                    double amount = Double.parseDouble(amountText);
                    // Perform your business logic here. For now, just display a Toast.
                    //  directPayment(amount);
                    startVoid(String.valueOf(amount));
                    Toast.makeText(getContext(), "Entered amount: " + amount, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        submitButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountText = amountEditText.getText().toString();
                if (!amountText.isEmpty()) {
                    double amount = Double.parseDouble(amountText);
                    // Perform your business logic here. For now, just display a Toast.
                    //  directPayment(amount);
                    paymentViaActivity(String.valueOf(amount));
                    Toast.makeText(getContext(), "Entered amount: " + amount, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // Build the void transaction

    private void paymentViaActivity(String amount){
        Log.d("Amount====>",""+amount);
        mPOSHandler.openPaymentActivity(
                getActivity() ,
                REQUEST_CODE_MAKE_PAYMENT ,
                amount ,
                UUID.randomUUID().toString()
        );
    }



    private void startVoid(String amount) {
        try {
            // Build the void request
            MyPOSVoid voidEx = MyPOSVoid.builder()
                    .STAN(27)
                    .authCode("VISSIM")
                    .dateTime("180129123753")
                    .voidLastTransactionFlag(true) // this may void last transaction initialized by this terminal
                    .build();

            // Start the void transaction
            if (getActivity() != null) {
                MyPOSAPI.openVoidActivity(getActivity(), voidEx, VOID_REQUEST_CODE, true);
            }
        } catch (FunctionalityNotSupportedException e) {
            Toast.makeText(getContext(), "Functionality not supported: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOID_REQUEST_CODE) {
            // The transaction was processed, handle the response
            if (resultCode == Activity.RESULT_OK) {
                // Something went wrong in the Payment core app and the result couldn't be returned properly
                if (data == null) {
                    Toast.makeText(getContext(), "Transaction cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }
                int transactionResult = data.getIntExtra("status", TransactionProcessingResult.TRANSACTION_FAILED);

                Toast.makeText(getContext(), "Void transaction has completed. Result: " + transactionResult, Toast.LENGTH_SHORT).show();

                // TODO: handle each transaction response accordingly
                if (transactionResult == TransactionProcessingResult.TRANSACTION_SUCCESS) {
                    // Transaction is successful
                }
            } else {
                // The user cancelled the transaction
                Toast.makeText(getContext(), "Transaction cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
