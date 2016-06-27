package com.parasoft.parabank.web.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.parasoft.parabank.domain.Account;

@SuppressWarnings("unchecked")
public class AccountsOverviewControllerTest extends AbstractBankControllerTest<AccountsOverviewController> {
    private void assertUserAccounts(final int id, final int expectedSize) throws Exception {
        request = registerSession(new MockHttpServletRequest(), id);
        final ModelAndView mav = processGetRequest(request, new MockHttpServletResponse());
        final List<Account> accounts = (List<Account>) getModelValue(mav, "accounts");
        assertEquals(expectedSize, accounts.size());
    }

    @Override
    public void onSetUp() throws Exception {
        super.onSetUp();
        setPath("/overview.htm");
        setFormName("none");
        registerSession(request);

    }

    @Test
    public void testHandleRequest() throws Exception {
        assertUserAccounts(12212, 11);
        assertUserAccounts(12323, 1);
        assertUserAccounts(3, 0);
    }
}
