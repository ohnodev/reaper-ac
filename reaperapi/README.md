<div align="center">
 <h1>ReaperAPI</h1>
    
 <div>
  <a href="https://github.com/ReaperAnticheat/ReaperAPI/actions/workflows/gradle-publish.yml">
   <img alt="Workflow" src="https://github.com/ReaperAnticheat/ReaperAPI/actions/workflows/gradle-publish.yml/badge.svg" />
  </a>
  <a href="https://repo.reaper.ac">
   <img alt="Maven repository" src="https://repo.reaper.ac/api/badge/latest/snapshots/ac/reaper/ReaperAPI?name=Version&style=flat">
  </a>
  <a href="https://discord.reaper.ac">
   <img alt="Discord" src="https://img.shields.io/discord/811396969670901800?style=flat&label=Discord&logo=discord">
  </a>
 </div>
 
 <br>
 <div>
  <a href="https://reaper.ac">Website</a>
  |
  <a href="https://github.com/ReaperAnticheat/Reaper/wiki/Developer-API">Wiki</a>
  |
  <a href="https://repo.reaper.ac/">Maven</a>
  |
  <a href="https://github.com/ReaperAnticheat/Reaper">ReaperAC</a>
 </div>

 <br>
 <div>
The official developer plugin API for ReaperAnticheat
 </div>

</div>

### **Requirements**:
- Java 17 or higher
- A supported environment listed [here](https://github.com/ReaperAnticheat/Reaper/wiki/Supported-environments)


### Wiki
You can find more documentation and examples of how to use the API on the [wiki](https://github.com/ReaperAnticheat/Reaper/wiki/Developer-API).

### **Gradle**:
```kt
repositories {
    maven {
        name = "reaperacSnapshots"
        url = uri("https://repo.reaper.ac/snapshots")
    }
}
dependencies {
    // replace %VERSION% with the latest API version
    compileOnly("ac.reaper:ReaperAPI:%VERSION%")
}
```

### **Maven**:
```xml
<repository>
  <id>reaperac-snapshots</id>
  <name>ReaperAC's Maven Repository</name>
  <url>https://repo.reaper.ac/snapshots</url>
</repository>
<!-- replace %VERSION% with the latest API version -->
<dependency>
  <groupId>ac.reaper</groupId>
  <artifactId>ReaperAPI</artifactId>
  <version>%VERSION%</version>
  <scope>provided</scope>
</dependency>
```
