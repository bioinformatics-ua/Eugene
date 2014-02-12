Building and Deploying Eugene with Java Web Start
=================================================


1. Retirar todas os certificados dos JARS da pasta MANIFEST
2. Criar e assinar o Plugins.jar e o Tools.jar
3. Configurar o projeto no NetBeans (Properties):

    * Application:    
            
            - Title: EuGene
            - Vendor: BioInformatics
            - Description: EuGene, gene optimization for heterologous expression.
            - Homepage: http://bioinformatics.ua.pt/eugene/
    
    * Web Start:
    
            - (check) Enable Web Start
            - Codebase: User defined (eg. HTTP deployment)
            - Codebase Preview: http://bioinformatics.ua.pt/EuGene/
            - (check) Allow offline
            - Signing -> Customize (inserir informação relativa ao certificado)
            - (select) Application descriptor (use project Main class)
    
Nota: Verificar se todos estes campos do launch.jnlp foram inseridos exatamente iguais a estes:
    * information:
            
            - <icon href="icon.png" kind="default"/>
            - <icon href="splash.png" kind="splash"/>
            - <shortcut online="false">
            -    <desktop/>
            -    <menu submenu="EuGene">
            -    </menu>
            - </shortcut>
            
    * resources:
    
            - <j2se version="1.6+"/>
            - ...
            - <jar href="Tools.jar"/>
            - <jar href="Plugins.jar"/>


Finalmente, fazer upload de tudo para o servidor
