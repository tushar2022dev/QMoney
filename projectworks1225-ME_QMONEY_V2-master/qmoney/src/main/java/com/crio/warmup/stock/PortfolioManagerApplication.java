package com.crio.warmup.stock;
import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {

    File f = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();
    PortfolioTrade[] trades = om.readValue(f, PortfolioTrade[].class);
    RestTemplate rt = new RestTemplate();
    List<TotalReturnsDto> ls = new ArrayList<TotalReturnsDto>();
    for(PortfolioTrade pf:trades)
    {
      //  LocalDate start = pf.getPurchaseDate();
       String sym = pf.getSymbol();
       LocalDate localDate = LocalDate.parse(args[1]);
       String Url = prepareUrl(pf,localDate,"209ac85df2915ec7ab39b5540baebb2eda5db14c");
       TiingoCandle[] tc = rt.getForObject( Url, TiingoCandle[].class);
       if(tc==null)
       {
         continue;
       }
       TotalReturnsDto temp = new TotalReturnsDto(sym,tc[tc.length-1].getClose());
       ls.add(temp);
    }
    Collections.sort(ls, new Comparator<TotalReturnsDto>() {
       @Override
       public int compare(TotalReturnsDto p1, TotalReturnsDto p2) {
           return (int)(p1.getClosingPrice().compareTo(p2.getClosingPrice()));
       }
   });
   List<String> ans = new ArrayList<>();
    for(int i=0;i<ls.size();i++)
    {
       ans.add(ls.get(i).getSymbol());
    }
    return ans;
 
    
   }

 
   // TODO:
   //  After refactor, make sure that the tests pass by using these two commands
   //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
   //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
   public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
 
     ObjectMapper om = getObjectMapper();
     PortfolioTrade[] pf = om.readValue(resolveFileFromResources(filename), PortfolioTrade[].class);
     List<PortfolioTrade> ls = Arrays.asList(pf);
     return ls; 
   }
 
 
   // TODO:
   //  Build the Url using given parameters and use this function in your code to cann the API.
   public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     String Url = "https://api.tiingo.com/tiingo/daily/"+trade.getSymbol()+"/prices?startDate="+trade.getPurchaseDate().toString()+"&endDate="+endDate+"&token="+token;
     return Url;
   }
 
   public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File f = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();
    PortfolioTrade[] trades = om.readValue(f, PortfolioTrade[].class);
    List<String> arr = new ArrayList<>();
    for(PortfolioTrade trade:trades)
    {
      arr.add(trade.getSymbol());
    }
    return arr;
    //  return Collections.emptyList();
  }
 
  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }
 
  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }
 
  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
 
 
 
 
  public static List<String> debugOutputs() {
 
    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/projectworks1225-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@2f9f7dcf";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "29";
 
 
   return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
       toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
       lineNumberFromTestFileInStackTrace});
 }
 
 


  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.




  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    //  return 0.0;
    return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {

    RestTemplate rt = new RestTemplate();
    String Url = prepareUrl(trade, endDate, token);
    TiingoCandle[] tc = rt.getForObject(Url, TiingoCandle[].class);
    return Arrays.asList(tc);
  }

  public static String getToken()
  {
    return "209ac85df2915ec7ab39b5540baebb2eda5db14c";
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
        File f = resolveFileFromResources(args[0]);
        ObjectMapper om = getObjectMapper();
        PortfolioTrade[] trades = om.readValue(f, PortfolioTrade[].class);
        List<AnnualizedReturn> ans = new ArrayList<>();
        LocalDate localDate = LocalDate.parse(args[1]);
        for(PortfolioTrade t:trades)
        {
          List<Candle> ls = fetchCandles(t, localDate, getToken());
          if(ls.size()==0)
          {
            continue;
          }
          ans.add(calculateAnnualizedReturns(localDate,t,getOpeningPriceOnStartDate(ls),getClosingPriceOnEndDate(ls)));
        }
        if(ans.size()==0)
        {
          return ans;
        }
        Collections.sort(ans,new Comparator<AnnualizedReturn>() {
          @Override
          public int compare(AnnualizedReturn a1,AnnualizedReturn a2)
          {
            return a2.getAnnualizedReturn().compareTo(a1.getAnnualizedReturn());
          }
        });
        System.out.println(ans);
     return ans;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {

        Double totalReturns = (sellPrice-buyPrice)/buyPrice;
        LocalDate purchase = trade.getPurchaseDate();
        Double noYears = purchase.until(endDate,ChronoUnit.DAYS)/365.24;
        Double annualized_returns = Math.pow(1+totalReturns, (1/noYears))-1;
      return new AnnualizedReturn(trade.getSymbol(),annualized_returns,totalReturns);
  }


  public static RestTemplate restTemplate = new RestTemplate();
  public static PortfolioManager pfm = PortfolioManagerFactory.getPortfolioManager(restTemplate);





  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    printJsonObject(mainCalculateSingleReturn(args));

    
    


  }
}
