package name.ljd.ch9_2.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.validator.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import name.ljd.ch9_2.domain.Person;

@Configuration
@EnableBatchProcessing//开启支持，千万不要忘记
public class CsvBatchConfig {
	
	@Bean
	public ItemReader<Person> reader() throws Exception{
		FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
		reader.setResource(new ClassPathResource("people.csv"));
			reader.setLineMapper(new DefaultLineMapper<Person>() {{
				setLineTokenizer(new DelimitedLineTokenizer() {{
					setNames(new String[] {"name","age","nation","address"});
				}});
				setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
					setTargetType(Person.class);
				}});
			}});
			return reader;
	}
	@Bean
	public ItemProcessor<Person,Person> processor(){
		CsvItemProcessor processor = new CsvItemProcessor();
		processor.setValidator(csvBeanValidator());
		return processor;
	}
	@Bean
	public ItemWriter<Person> writor(DataSource dataSource){//已有bean以参数注入
		JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
		String sql = "insert into person " + "(id,name,age,nation,address) "
		+ "values(hibernate_sequence.nextval,:name,:age,:nation,:address)";
		writer.setSql(sql);
		writer.setDataSource(dataSource);
		return writer;
	}
	@Bean
	public JobRepository jobRepository(DataSource dataSource,PlatformTransactionManager transactionManager) 
	throws Exception{
		JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
		jobRepositoryFactoryBean.setDatabaseType("oracle");
		jobRepositoryFactoryBean.setDataSource(dataSource);
		jobRepositoryFactoryBean.setTransactionManager(transactionManager);
		jobRepositoryFactoryBean.setIsolationLevelForCreate("ISOLATION_READ_UNCOMMITTED");
		return jobRepositoryFactoryBean.getObject();
	}
	@Bean 
	public SimpleJobLauncher jobLanucher(DataSource dataSource,
			PlatformTransactionManager transactionManager) throws Exception{
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository(dataSource, transactionManager));
		return jobLauncher;
	}
	@Bean
	public Job importJob(JobBuilderFactory jobs,Step s1) {
		return jobs.get("importJob")
				.incrementer(new RunIdIncrementer())
				.flow(s1)
				.end()
				.listener(csvJobListener())
				.build();
	}
	@Bean
	public CsvJobListener csvJobListener() {
		return new CsvJobListener();
	}
	@Bean 
	public Validator<Person> csvBeanValidator(){
		return new CsvBeanValidator<Person>();
	}
	@Bean
	public Step step1(StepBuilderFactory stepBuilderFactory,ItemReader<Person> reader,
			ItemWriter<Person> writer,ItemProcessor<Person, Person> processor) {
		return stepBuilderFactory
				.get("step1")
				.<Person,Person>chunk(65000)//每次提交的数据
				.reader(reader)
				.processor(processor)
				.writer(writer)
				.build();
	}
}
