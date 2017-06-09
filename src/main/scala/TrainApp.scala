import org.template.complementarypurchase.DataSourceParams
import org.template.complementarypurchase.AlgorithmParams
import io.prediction.controller.{EmptyParams, EngineParams}
import io.prediction.data.storage.EngineInstance
import io.prediction.workflow.CreateWorkflow.WorkflowConfig
import io.prediction.workflow._
import org.joda.time.DateTime
import org.template.complementarypurchase.ComplementaryPurchaseEngine



//import breeze.linalg.rank

object TrainApp extends App {

  // WTF: envs must not be empty or CreateServer.engineInstances.get... fails due to JDBCUtils.stringToMap
  val envs = Map("FOO" -> "BAR")

  val sparkEnv = Map("spark.master" -> "local")

  // WTF: envs must not be empty or CreateServer.engineInstances.get... fails due to JDBCUtils.stringToMap
  val sparkConf = Map("spark.executor.extraClassPath" -> ".")

  val engineFactoryName = "org.template.complementarypurchase.ComplementaryPurchaseEngine"


  val workflowConfig = WorkflowConfig(
    engineId = EngineConfig.engineId,
    engineVersion = EngineConfig.engineVersion,
    engineVariant = EngineConfig.engineVariantId,
    engineFactory = engineFactoryName
  )

  val workflowParams = WorkflowParams(
    verbose = workflowConfig.verbosity,
    skipSanityCheck = workflowConfig.skipSanityCheck,
    stopAfterRead = workflowConfig.stopAfterRead,
    stopAfterPrepare = workflowConfig.stopAfterPrepare,
    sparkEnv = WorkflowParams().sparkEnv ++ sparkEnv
  )

  WorkflowUtils.modifyLogging(workflowConfig.verbose)

  val dataSourceParams = DataSourceParams(sys.env.get("APP_NAME").get)
  val preparatorParams = EmptyParams()
 // val preparatorParams =PreparatorParams(nGram = 2, numFeatures = 500)
 
  val algorithmParamsList = Seq("algo"-> AlgorithmParams(basketWindow = 120, maxRuleLength = 2, minSupport=0.001, minConfidence =0.1, minLift=1.0, minBasketSize=2, maxNumRulesPerCond=5 ))
  val servingParams = EmptyParams()

  val engineInstance = EngineInstance(
    id = "",
    status = "INIT",
    startTime = DateTime.now,
    endTime = DateTime.now,
    engineId = workflowConfig.engineId,
    engineVersion = workflowConfig.engineVersion,
    engineVariant = workflowConfig.engineVariant,
    engineFactory = workflowConfig.engineFactory,
    batch = workflowConfig.batch,
    env = envs,
    sparkConf = sparkConf,
    dataSourceParams = JsonExtractor.paramToJson(workflowConfig.jsonExtractor, workflowConfig.engineParamsKey -> dataSourceParams),
    preparatorParams = JsonExtractor.paramToJson(workflowConfig.jsonExtractor, workflowConfig.engineParamsKey -> preparatorParams),
    algorithmsParams = JsonExtractor.paramsToJson(workflowConfig.jsonExtractor, algorithmParamsList),
    servingParams = JsonExtractor.paramToJson(workflowConfig.jsonExtractor, workflowConfig.engineParamsKey -> servingParams)
  )

  val (engineLanguage, engineFactory) = WorkflowUtils.getEngine(engineInstance.engineFactory, getClass.getClassLoader)

  val engine = engineFactory()

  val engineParams = EngineParams(
    dataSourceParams = dataSourceParams,
    preparatorParams = preparatorParams,
    algorithmParamsList = algorithmParamsList,
    servingParams = servingParams
  )

  val engineInstanceId = CreateServer.engineInstances.insert(engineInstance)

  CoreWorkflow.runTrain(
    env = envs,
    params = workflowParams,
    engine = engine,
    engineParams = engineParams,
    engineInstance = engineInstance.copy(id = engineInstanceId)
  )

  CreateServer.actorSystem.shutdown()

}


