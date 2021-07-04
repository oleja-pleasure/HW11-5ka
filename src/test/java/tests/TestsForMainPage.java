package tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import config.Credentials;
import helpers.Attach;
import io.qameta.allure.Description;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static helpers.Attach.getSessionId;
import static helpers.GetProperty.readProperty;
import static io.qameta.allure.Allure.step;

public class TestsForMainPage {

    String good = "хлебушек",
            newCity = "г. Казань",
            city = "г. Москва",
            nameOfGood,
            rub,
            kop;

    @BeforeAll
    static void setUpConfig() {
        String login = Credentials.credentials.login();
        String password = Credentials.credentials.password();
        String server = readProperty();
        Configuration.remote = String.format("https://%s:%s@%s/wd/hub", login, password, server);
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
        Configuration.browser = "chrome";
        Configuration.startMaximized = true;

        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("enableVideo", true);

        Configuration.browserCapabilities = capabilities;
    }

    @Test
    @Description("Проверка текста при нулевом результате поиска")
    void checkTextWithNullResult() {
        step("Открытие сайта", () ->
                open("https://5ka.ru/"));
        step("Открытие вкладки 'Товары по акциям'", () ->
                $(".header-menu__top__link_item").click());
        step("Поиск товара", () ->
                $(".search_input").val(good).pressEnter());
        step("Проверка текста", () ->
                $(".special-offers__no-offers").shouldHave(text("Акций по запросу \"" + good + "\" не найдено.")));
    }

    @Test
    @Description("Проверка смены города")
    void changeCity() {
        step("Открытие сайта", () ->
                open("https://5ka.ru/"));
        step("Проверка города", () ->
                $(".page-title__text").shouldHave(text("Акции в " + city)));
        step("Клик на ссылку 'Изменить город'", () ->
                $(".special-offers__location-change").click());
        step("Выбор города", () -> {
            $(".search__input").val(newCity);
            $$(".items").findBy(text(newCity)).click();
        });
        step("Проверка выбранного города", () ->
                $(".page-title__text").shouldHave(text("Акции в " + newCity)));
    }

    @Test
    @Description("Проверка фильтра 'Цена по акции'")
    void checkPriceFilter() {
        step("Открытие сайта", () ->
                open("https://5ka.ru/"));
        step("Открытие вкладки 'Товары по акциям'", () ->
                $(".header-menu__top__link_item").click());
        step("Ввод значения минимальной цены в поле 'от'", () -> {
            String price = $(".sale-card__price .sale-card__price--new").getText();
            rub=price.substring(0,(price.length()-2));
            kop=price.substring((price.length()-2));
            nameOfGood = $(".sale-card__title").getText();
            $("[type='number']").val(rub + "." + kop).pressEnter();
        });
        step("Проверка наличия товара с минимальной ценой", () ->
                $(".sale-card__title").shouldHave(exactText(nameOfGood)));
    }

    @AfterEach
    void attach() {
        String sessionId = getSessionId();
        Attach.screenshotAs("Last screenshot");
        Attach.pageSource();
        Attach.browserConsoleLogs();
        Attach.addVideo(sessionId);
    }

    @AfterAll
    static void closeBrowser() {
        closeWebDriver();
    }
}