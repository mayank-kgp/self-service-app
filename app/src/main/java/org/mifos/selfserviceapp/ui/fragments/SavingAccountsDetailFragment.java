package org.mifos.selfserviceapp.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.mifos.selfserviceapp.R;
import org.mifos.selfserviceapp.api.local.PreferencesHelper;
import org.mifos.selfserviceapp.models.accounts.savings.SavingsWithAssociations;
import org.mifos.selfserviceapp.models.accounts.savings.Status;
import org.mifos.selfserviceapp.presenters.SavingAccountsDetailPresenter;
import org.mifos.selfserviceapp.ui.activities.base.BaseActivity;
import org.mifos.selfserviceapp.ui.enums.AccountType;
import org.mifos.selfserviceapp.ui.enums.ChargeType;
import org.mifos.selfserviceapp.ui.fragments.base.BaseFragment;
import org.mifos.selfserviceapp.ui.views.SavingAccountsDetailView;
import org.mifos.selfserviceapp.utils.CircularImageView;
import org.mifos.selfserviceapp.utils.Constants;
import org.mifos.selfserviceapp.utils.DateHelper;
import org.mifos.selfserviceapp.utils.QrCodeGenerator;
import org.mifos.selfserviceapp.utils.SymbolsUtils;
import org.mifos.selfserviceapp.utils.Toaster;
import org.mifos.selfserviceapp.utils.Utils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Vishwajeet
 * @since 18/8/16.
 */

public class SavingAccountsDetailFragment extends BaseFragment implements SavingAccountsDetailView {

    @BindView(R.id.tv_account_status)
    TextView tvAccountStatus;

    @BindView(R.id.iv_circle_status)
    CircularImageView ivCircularStatus;

    @BindView(R.id.tv_total_withdrawals)
    TextView tvTotalWithDrawals;

    @BindView(R.id.tv_min_req_bal)
    TextView tvMiniRequiredBalance;

    @BindView(R.id.tv_account_balance)
    TextView tvAccountBalance;

    @BindView(R.id.tv_saving_account_number)
    TextView tvSavingAccountNumber;

    @BindView(R.id.tv_nominal_interest_rate)
    TextView tvNominalInterestRate;

    @BindView(R.id.tv_total_deposits)
    TextView tvTotalDeposits;

    @BindView(R.id.tv_acc_balance)
    TextView tvAccountBalanceMain;

    @BindView(R.id.tv_last_transaction)
    TextView tvLastTransaction;

    @BindView(R.id.made_on)
    TextView tvMadeOnTextView;

    @BindView(R.id.tv_made_on)
    TextView tvMadeOnTransaction;

    @BindView(R.id.ll_account)
    LinearLayout layoutAccount;

    @BindView(R.id.ll_error)
    View layoutError;

    @BindView(R.id.tv_status)
    TextView tvStatus;

    @BindView(R.id.iv_qr_code)
    ImageView ivQrCode;

    @Inject
    PreferencesHelper preferencesHelper;

    @Inject
    SavingAccountsDetailPresenter mSavingAccountsDetailPresenter;

    private View rootView;
    private long savingsId;
    private Status status;

    public static SavingAccountsDetailFragment newInstance(long savingsId) {
        SavingAccountsDetailFragment fragment = new SavingAccountsDetailFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.SAVINGS_ID, savingsId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            savingsId = getArguments().getLong(Constants.SAVINGS_ID);
        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_saving_account_details, container, false);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        setToolbarTitle(getString(R.string.saving_account_details));
        ButterKnife.bind(this, rootView);
        mSavingAccountsDetailPresenter.attachView(this);

        mSavingAccountsDetailPresenter.loadSavingsWithAssociations(savingsId);

        return rootView;
    }

    @OnClick(R.id.tv_help_line_number)
    void dialHelpLineNumber() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + getString(R.string.help_line_number)));
        startActivity(intent);
    }

    @OnClick(R.id.tv_deposit)
    void deposit() {
        if (status.getActive()) {
            ((BaseActivity) getActivity()).replaceFragment(SavingsMakeTransferFragment
                    .newInstance(savingsId, Constants.TRANSFER_PAY_TO), true, R.id.container);
        } else {
            Toaster.show(rootView, getString(R.string.account_not_active_to_perform_deposit));
        }
    }

    @OnClick(R.id.tv_make_a_transfer)
    void transfer() {
        if (status.getActive()) {
            ((BaseActivity) getActivity()).replaceFragment(SavingsMakeTransferFragment
                    .newInstance(savingsId, Constants.TRANSFER_PAY_FROM), true, R.id.container);
        } else {
            Toaster.show(rootView, getString(R.string.account_not_active_to_perform_transfer));
        }
    }

    @Override
    public void showSavingAccountsDetail(SavingsWithAssociations savingsWithAssociations) {
        layoutAccount.setVisibility(View.VISIBLE);

        String currencySymbol = savingsWithAssociations.getCurrency().getDisplaySymbol();
        Double accountBalance = savingsWithAssociations.getSummary().getAccountBalance();

        tvAccountStatus.setText(savingsWithAssociations.getClientName());
        tvMiniRequiredBalance.setText(getString(R.string.double_and_String,
                savingsWithAssociations.getMinRequiredOpeningBalance(), currencySymbol));
        tvTotalWithDrawals.setText(getString(R.string.double_and_String,
                savingsWithAssociations.getSummary().getTotalWithdrawals(), currencySymbol));
        tvAccountBalance.setText(
                getString(R.string.double_and_String, accountBalance, currencySymbol));
        tvAccountBalanceMain.setText(
                getString(R.string.double_and_String, accountBalance, currencySymbol));
        tvNominalInterestRate.setText(getString(R.string.double_and_String,
                savingsWithAssociations.getNominalAnnualInterestRate(), SymbolsUtils.PERCENT));
        tvSavingAccountNumber.setText(String.valueOf(savingsWithAssociations.getAccountNo()));
        tvTotalDeposits.setText(getString(R.string.double_and_String,
                savingsWithAssociations.getSummary().getTotalDeposits(), currencySymbol));

        if (!savingsWithAssociations.getTransactions().isEmpty()) {
            tvLastTransaction.setText(getString(R.string.double_and_String,
                    savingsWithAssociations.getTransactions().get(0).getAmount(), currencySymbol));
            tvMadeOnTransaction.setText(DateHelper.getDateAsString(
                    savingsWithAssociations.getLastActiveTransactionDate()));
        } else {
            tvLastTransaction.setText(R.string.no_transaction);
            tvMadeOnTransaction.setVisibility(View.GONE);
            tvMadeOnTextView.setVisibility(View.GONE);
        }

        String accountDetails = QrCodeGenerator.getAccountDetailsInString(savingsWithAssociations.
                getAccountNo(), preferencesHelper.getOfficeName(), AccountType.SAVINGS);
        ivQrCode.setImageBitmap(QrCodeGenerator.encodeAsBitmap(accountDetails));

        showAccountStatus(savingsWithAssociations);
    }

    @Override
    public void showErrorFetchingSavingAccountsDetail(String message) {
        layoutAccount.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        tvStatus.setText(message);
    }

    @Override
    public void showAccountStatus(SavingsWithAssociations savingsWithAssociations) {
        status = savingsWithAssociations.getStatus();
        if (status.getActive()) {
            ivCircularStatus.setImageDrawable(
                    Utils.setCircularBackground(R.color.deposit_green, getActivity()));
            tvAccountStatus.setText(R.string.active);
        } else if (status.getApproved()) {
            ivCircularStatus.setImageDrawable(
                    Utils.setCircularBackground(R.color.blue, getActivity()));
            tvAccountStatus.setText(R.string.need_approval);
        } else if (status.getSubmittedAndPendingApproval()) {
            ivCircularStatus.setImageDrawable(
                    Utils.setCircularBackground(R.color.light_yellow, getActivity()));
            tvAccountStatus.setText(R.string.pending);
        } else if (status.getMatured()) {
            ivCircularStatus.setImageDrawable(
                    Utils.setCircularBackground(R.color.red_light, getActivity()));
            tvAccountStatus.setText(R.string.matured);
        } else {
            ivCircularStatus.setImageDrawable(
                    Utils.setCircularBackground(R.color.black, getActivity()));
            tvAccountStatus.setText(R.string.closed);
        }
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void showProgress() {
        layoutAccount.setVisibility(View.GONE);
        showProgressBar();
    }

    @Override
    public void hideProgress() {
        layoutAccount.setVisibility(View.VISIBLE);
        hideProgressBar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideProgressBar();
        mSavingAccountsDetailPresenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_saving_account, menu);

        if (status != null && status.getActive()) {
            menu.findItem(R.id.item_savings_charges).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_transactions:
                ((BaseActivity) getActivity()).replaceFragment(SavingAccountsTransactionFragment.
                                newInstance(savingsId), true, R.id.container);
                break;
            case R.id.item_savings_charges:
                ((BaseActivity) getActivity()).replaceFragment(ClientChargeFragment.
                        newInstance(savingsId, ChargeType.SAVINGS), true, R.id.container);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
